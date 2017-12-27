import { Component, Inject } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { marker } from 'leaflet';
import { isArray, isPlainObject } from 'lodash';
import * as moment from 'moment';

import { EntityChanges, HistoryTimeEntries } from '../../../shared/history';
import { AjaxProtocol } from '../../ajax/AjaxProtocol';
import * as entities from './entities';
import { JsonIdentifier, VisualEntity, VisualOptions } from './entities/decorators';
import { Entity } from './entities/Entity';

import { IntervalObservable } from '../../rxjs/observable/IntervalObservable';
import { takeWhile } from '../../rxjs/operators';

enum STATE {
    LOADING = 1,

    START = 2,
    PAUSED = 4,
    END = 8,

    FORWARD = 16,
    REWIND = 32,

    NOT_RUNNING = START | PAUSED | END, // don't include the loading state
    RUNNING = FORWARD | REWIND,
}

enum STEP {
    NONE,
    FORWARD,
    BACKWARD
}

@Component({
    selector: 'visualization',
    template: require('./visualization.component.html'),
    styles: [require('./visualization.component.css')],
})
export class VisualizationComponent {

    readonly STATE = STATE;

    readonly TICK = 200; // milliseconds

    readonly NO_EMIT = { emitEvent: false };

    mainActionIcon: string;
    speedControl: FormControl;
    state: STATE;

    private speed: number;
    private time: number;
    private timeEntryIndex: number;
    private nChangeFiles: number;
    private changeFileIndex: number;
    private lastStep: STEP;

    private entities: {
        [key: string]: {
            [key: number]: Entity
        }
    };

    private timeEntries: {
        previous: HistoryTimeEntries | null,
        current: HistoryTimeEntries,
        next: HistoryTimeEntries | null,
    };

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {}

    ngOnInit() {
        this.speed = 10;
        this.speedControl = new FormControl(this.speed, [
            Validators.required,
            Validators.pattern(/-?[1-9][0-9]*/)
        ]);

        this.updateState(STATE.LOADING);

        this.speedControl.valueChanges.subscribe((value) => {
            if (this.speedControl.errors) return;
            this.speed = parseInt(value);
        });

        this.load()
            .then(() => this.updateState(STATE.START))
            .catch((error) => console.error(error));
    }

    getJsonIdentifier(EntityConstructor: Function): string {
        if (!Reflect.hasOwnMetadata(JsonIdentifier, EntityConstructor)) {
            throw new Error(`No json identifier found on ${EntityConstructor.name}`);
        }
        return Reflect.getOwnMetadata(JsonIdentifier, EntityConstructor);
    }

    async load(): Promise<void> {
        await this.ajax.history.init('history');
        await this.createEntities();

        this.nChangeFiles = await this.ajax.history.numberOFChangeFiles();

        this.changeFileIndex = 0;

        this.timeEntries = {
            previous: null,
            current: await this.ajax.history.getChangeFile(this.changeFileIndex),
            next: await this.ajax.history.getChangeFile(this.changeFileIndex + 1),
        };
    }

    async createEntities(): Promise<void> {
        const entitySource = await this.ajax.history.readEntities();

        this.entities = {};

        Object.values(entities).forEach((EntityConstructor) => {
            const jsonIdentifier = this.getJsonIdentifier(EntityConstructor);
            const visualOptions: VisualOptions = Reflect.getOwnMetadata(VisualEntity, EntityConstructor);

            if (!(jsonIdentifier in entitySource)) return;

            this.entities[jsonIdentifier] = {};

            entitySource[jsonIdentifier].forEach((jsonEntity) => {
                const entity: Entity = Reflect.construct(EntityConstructor, [jsonEntity]);
                this.entities[jsonIdentifier][entity.id] = entity;

                if (visualOptions) {
                    Reflect.defineMetadata(VisualEntity, marker([0, 0]), entity);
                }
            });
        });
    }

    applyChange(entity: any, data: EntityChanges, from: 'old' | 'new') {
        Object.defineProperty(data, 'id', { enumerable: false });
        Object.keys(data).forEach((name) => {
            let property = data[name][from];

            if (isPlainObject(property) && 'type' in property && 'id' in property) {
                if (isArray(property.id)) {
                    property = property.id.map((id: number) => this.entities[property.type][id] || null);
                } else {
                    property = this.entities[property.type][property.id] || null;
                }
            }

            entity[name] = property;
        });
    }

    is(...states: Array<STATE>): boolean;
    is(...states: Array<[STATE, boolean]>): boolean;
    is(...states: Array<STATE | [STATE, boolean]>): boolean {
        if (Array.isArray(states[0])) {
            return states.reduce((r, v) => {
                v = v as [STATE, boolean];
                return r || (this.is(v[0]) && v[1]);
            }, false);
        }

        return (this.state & states.reduce((r, v) => r | v as STATE, 0)) > 0;
    }

    togglePlayPause() {
        if (this.is(STATE.NOT_RUNNING)) {
            this.updateState(this.speed > 0 ? STATE.FORWARD : STATE.REWIND);
        } else {
            this.updateState(STATE.PAUSED);
        }
    }

    updateState(state: STATE) {
        console.log(`${STATE[this.state]} -> ${STATE[state]}`);

        this.state = state;

        if (this.is(STATE.LOADING, STATE.NOT_RUNNING)) {
            this.mainActionIcon = 'play';
        }

        if (this.is(STATE.RUNNING)) {
            this.mainActionIcon = 'pause';
        }

        if (this.is(STATE.LOADING, STATE.RUNNING)) {
            this.speedControl.disable(this.NO_EMIT);
        }

        if (this.is(STATE.NOT_RUNNING)) {
            this.speedControl.enable(this.NO_EMIT);
        }

        if (this.is(STATE.FORWARD)) {
            IntervalObservable
                .create(this.TICK)
                .pipe(takeWhile(() => this.state === STATE.FORWARD))
                .subscribe(() => this.onTick());
        }

        if (this.is(STATE.REWIND)) {
            IntervalObservable
                .create(this.TICK)
                .pipe(takeWhile(() => this.state === STATE.REWIND))
                .subscribe(() => this.onTick());
        }

        if (this.is(STATE.START)) {
            this.timeEntryIndex = 0;
            this.time = 0;
            this.lastStep = STEP.NONE;
        }

        if (this.is(STATE.END)) {
            this.timeEntryIndex = this.timeEntries.current.length - 1;
            this.time = this.timeEntries.current[this.timeEntryIndex].time;
            this.lastStep = STEP.NONE;
        }
    }

    changeSpeed(n: number) {
        this.speed += (this.speed + n === 0) ? 2 * n : n;

        if (this.is([STATE.FORWARD, this.speed < 0])) {
            this.updateState(STATE.REWIND);
        } else if (this.is([STATE.REWIND, this.speed > 0])) {
            this.updateState(STATE.FORWARD);
        }

        this.speedControl.setValue(this.speed, this.NO_EMIT);
    }

    stepForward() {
        if (this.is(STATE.START)) this.updateState(STATE.PAUSED);
        const timeEntry = this.next();
        this.time = timeEntry.time;
        this.forwardEntry(timeEntry);
        this.increaseIndex();
    }

    stepBackward() {
        if (this.is(STATE.END)) this.updateState(STATE.PAUSED);
        const timeEntry = this.previous();
        this.time = timeEntry.time;
        this.rewindEntry(timeEntry);
        this.decreaseIndex();
    }

    increaseIndex() {
        if (++this.timeEntryIndex === this.timeEntries.current.length) {
            if (this.timeEntries.next) {
                this.timeEntries.previous = this.timeEntries.current;
                this.timeEntries.current = this.timeEntries.next;
                this.timeEntryIndex = 0;
                if (++this.changeFileIndex < this.nChangeFiles - 1) {
                    this.ajax.history.getChangeFile(this.changeFileIndex + 1).then((entry) => this.timeEntries.next = entry);
                } else {
                    this.timeEntries.next = null;
                }
            } else {
                this.updateState(STATE.END);
            }
        }
    }

    decreaseIndex() {
        if (--this.timeEntryIndex === -1) {
            if (this.timeEntries.previous) {
                this.timeEntries.next = this.timeEntries.current;
                this.timeEntries.current = this.timeEntries.previous;
                this.timeEntryIndex = this.timeEntries.current.length - 1;
                if (--this.changeFileIndex > 0) {
                    this.ajax.history
                        .getChangeFile(this.changeFileIndex - 1)
                        .then((entry) => this.timeEntries.previous = entry);
                } else {
                    this.timeEntries.previous = null;
                }
            } else {
                this.updateState(STATE.START);
            }
        }
    }

    next(): HistoryTimeEntries[0] {
        if (this.lastStep === STEP.BACKWARD) this.increaseIndex();
        this.lastStep = STEP.FORWARD;
        return this.timeEntries.current[this.timeEntryIndex];
    }

    previous(): HistoryTimeEntries[0] {
        if (this.lastStep === STEP.FORWARD) this.decreaseIndex();
        this.lastStep = STEP.BACKWARD;
        return this.timeEntries.current[this.timeEntryIndex];
    }

    forwardEntry(timeEntry: HistoryTimeEntries[0]) {
        for (let i = 0; i < timeEntry.events.length; i++) {
            const event = timeEntry.events[i];
            Object.keys(event.changes).forEach((jsonIdentifier) => {
                event.changes[jsonIdentifier].forEach((changes) => {
                    const entity = this.entities[jsonIdentifier][changes.id];
                    this.applyChange(entity, changes, 'new');
                });
            });
        }
    }

    rewindEntry(timeEntry: HistoryTimeEntries[0]) {
        for (let i = timeEntry.events.length - 1; i >= 0; i--) {
            const event = timeEntry.events[i];
            Object.keys(event.changes).forEach((jsonIdentifier) => {
                event.changes[jsonIdentifier].forEach((changes) => {
                    const entity = this.entities[jsonIdentifier][changes.id];
                    this.applyChange(entity, changes, 'old');
                });
            });
        }
    }

    onTick() {
        const nextTime = this.time + this.speed * this.TICK / 1000;

        let timeEntry: HistoryTimeEntries[0];

        if (this.speed > 0) {
            timeEntry = this.next();
            while (timeEntry.time < nextTime) {
                this.forwardEntry(timeEntry);
                this.increaseIndex();
                if (this.state !== STATE.FORWARD) return;
                timeEntry = this.next();
            }
        } else {
            timeEntry = this.previous();
            while (timeEntry.time > nextTime) {
                this.rewindEntry(timeEntry);
                this.decreaseIndex();
                if (this.state !== STATE.REWIND) return;
                timeEntry = this.previous();
            }
        }

        this.time = nextTime;
    }

    get formattedTime() {
        return moment.unix(this.time).utc().format('HH:mm:ss');
    }
}
