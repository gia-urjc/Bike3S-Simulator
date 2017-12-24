import { Component, Inject } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { marker } from 'leaflet';
import { isArray, isPlainObject } from 'lodash';

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

    NOT_RUNNING = 2 | 4 | 8,
    RUNNING = 16 | 32,
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
        await this.createEntities();

        this.timeEntries = {
            previous: null,
            current: await this.ajax.history.nextChangeFile(),
            next: null,
        };
    }

    async createEntities(): Promise<void> {
        await this.ajax.history.init('history');
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
        console.log(this.time, `${STATE[this.state]} -> ${STATE[state]}`, this);

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
                .subscribe(() => this.onTickForward());
        }

        if (this.is(STATE.REWIND)) {
            IntervalObservable
                .create(this.TICK)
                .pipe(takeWhile(() => this.state === STATE.REWIND))
                .subscribe(() => this.onTickRewind());
        }

        if (this.is(STATE.START)) {
            this.timeEntryIndex = 0;
            this.time = 0;
            this.state = STATE.LOADING;
            this.ajax.history.nextChangeFile().then((entry) => {
                this.timeEntries.next = entry;
                this.state = STATE.START;
            });
        }

        if (this.is(STATE.END)) {
            this.timeEntryIndex = this.timeEntries.current.length - 1;
            this.time = this.timeEntries.current[this.timeEntryIndex].time;
            this.state = STATE.LOADING;
            this.ajax.history.previousChangeFile().then((entry) => {
                this.timeEntries.previous = entry;
                this.state = STATE.END;
            });
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

    forwardTimeEntry(timeEntry: HistoryTimeEntries[0]) {
        for (let i = 0; i < timeEntry.events.length; i++) {
            const event = timeEntry.events[i];
            console.log(timeEntry.time, event.name);
            Object.keys(event.changes).forEach((jsonIdentifier) => {
                event.changes[jsonIdentifier].forEach((changes) => {
                    const entity = this.entities[jsonIdentifier][changes.id];
                    this.applyChange(entity, changes, 'new');
                });
            });
        }
    }

    rewindTimeEntry(timeEntry: HistoryTimeEntries[0]) {
        for (let i = timeEntry.events.length - 1; i >= 0; i--) {
            const event = timeEntry.events[i];
            console.log(timeEntry.time, event.name);
            Object.keys(event.changes).forEach((jsonIdentifier) => {
                event.changes[jsonIdentifier].forEach((changes) => {
                    const entity = this.entities[jsonIdentifier][changes.id];
                    this.applyChange(entity, changes, 'old');
                });
            });
        }
    }

    stepForward() {
        const timeEntry = this.timeEntries.current[this.timeEntryIndex++];
        this.forwardTimeEntry(timeEntry);
        this.time = timeEntry.time;
    }

    stepRewind() {
        const timeEntry = this.timeEntries.current[this.timeEntryIndex--];
        this.rewindTimeEntry(timeEntry);
        this.time = timeEntry.time;
    }

    onTickForward() {
        const nextTime = this.time + this.speed * this.TICK / 1000;

        let timeEntry = this.timeEntries.current[this.timeEntryIndex];

        while (timeEntry && timeEntry.time < nextTime) {
            this.forwardTimeEntry(timeEntry);

            timeEntry = this.timeEntries.current[++this.timeEntryIndex];

            if (timeEntry) continue;

            if (this.timeEntries.next) {
                this.timeEntries.previous = this.timeEntries.current;
                this.timeEntries.current = this.timeEntries.next;
                this.ajax.history.nextChangeFile()
                    .then((entry) => this.timeEntries.next = entry)
                    .catch(() => this.timeEntries.next = null);
                this.timeEntryIndex = 0;
                timeEntry = this.timeEntries.current[this.timeEntryIndex];
            } else {
                return this.updateState(STATE.END);
            }
        }

        this.time = nextTime;
    }

    onTickRewind() {
        const nextTime = this.time + this.speed * this.TICK / 1000;

        let timeEntry = this.timeEntries.current[this.timeEntryIndex];

        while (timeEntry && timeEntry.time > nextTime) {
            this.rewindTimeEntry(timeEntry);

            timeEntry = this.timeEntries.current[--this.timeEntryIndex];

            if (timeEntry) continue;

            if (this.timeEntries.previous) {
                this.timeEntries.next = this.timeEntries.current;
                this.timeEntries.current = this.timeEntries.previous;
                this.ajax.history.previousChangeFile()
                    .then((entry) => this.timeEntries.previous = entry)
                    .catch(() => this.timeEntries.previous = null);
                this.timeEntryIndex = this.timeEntries.current.length ? this.timeEntries.current.length - 1 : 0;
                timeEntry = this.timeEntries.current[this.timeEntryIndex];
            } else {
                return this.updateState(STATE.START);
            }
        }

        this.time = nextTime;
    }
}
