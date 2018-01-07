import { Component, Inject } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { Layer, Marker, Popup } from 'leaflet';
import { isArray, isEqual } from 'lodash';
import * as moment from 'moment';

import { IntervalObservable } from '../../rxjs/observable/IntervalObservable';
import { takeWhile } from '../../rxjs/operators';

import { EntityChanges, HistoryTimeEntry, IdReference, isIdReference } from '../../../shared/history';
import { Geo, safe } from '../../../shared/util';
import { AjaxProtocol } from '../../ajax/AjaxProtocol';
import { Entity, Visual, VisualConfiguration } from './entities/Entity';
import { breakPoint, LeafletUtil } from './util';

import * as EntityConstructors from './entities';

interface VisualMetadata {
    marker: Marker,
    route: Geo.Route | null,
    speed: number,
    routeIndex: number,
    distances: Array<number>,
    distanceFromLastPoint: number,
    popup: Popup,
}

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

@Component({
    selector: 'visualization',
    template: require('./visualization.component.html'),
    styles: [require('./visualization.component.css')],
})
export class Visualization {

    private static activeLayers: Set<Layer>;

    readonly STATE = STATE; // make STATE available in template

    readonly TICK = 200; // milliseconds

    readonly NO_EMIT = { emitEvent: false };

    mainActionIcon: string;
    speedControl: FormControl;
    state: STATE;
    fromEnd: boolean;

    private speed: number;
    private time: number;
    private timeEntryIndex: number;
    private nChangeFiles: number;
    private changeFileIndex: number;
    private movingEntities: Array<Entity>;
    private activeLayers: Set<Layer>;

    private entities: {
        [key: string]: {
            [key: number]: Entity
        }
    };

    private timeEntries: {
        previous: Array<HistoryTimeEntry> | null,
        current: Array<HistoryTimeEntry>,
        next: Array<HistoryTimeEntry> | null,
    };

    static hasLayer(layer: Layer) {
        return this.activeLayers.has(layer);
    }

    static addLayer(layer: Layer) {
        return this.activeLayers.add(layer);
    }

    static deleteLayer(layer: Layer) {
        return this.activeLayers.delete(layer);
    }

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {}

    ngOnInit() {
        this.activeLayers = Visualization.activeLayers = new Set();
        this.movingEntities = [];
        this.fromEnd = false;

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

    async load(): Promise<void> {
        await this.ajax.history.init('history');
        await this.createEntities();

        this.nChangeFiles = await this.ajax.history.numberOFChangeFiles();

        this.changeFileIndex = 0;

        this.timeEntries = {
            previous: null,
            current: await this.ajax.history.getChangeFile(this.changeFileIndex),
            next: this.nChangeFiles > 1 ? await this.ajax.history.getChangeFile(this.changeFileIndex + 1) : null,
        };
    }

    async createEntities(): Promise<void> {
        const deferredReferences = new Map<Entity, Array<[string, IdReference]>>();

        this.entities = {};

        for (let Constructor of Object.values(EntityConstructors)) {
            const configuration: VisualConfiguration = Reflect.getOwnMetadata(Visual, Constructor);
            const entitySource = await this.ajax.history.getEntities(configuration.jsonIdentifier);

            this.entities[configuration.jsonIdentifier] = {};

            entitySource.instances.forEach((jsonEntity) => {
                const entity = Reflect.construct(Constructor, []);

                entitySource.prototype.forEach((property) => entity[property] = null);

                Object.entries(jsonEntity).forEach(([property, value]) => {
                    if (isIdReference(value)) {
                        const references = deferredReferences.get(entity) || [];
                        references.push([property, value]);
                        deferredReferences.set(entity, references);
                    } else {
                        const onChange = safe(configuration, `onChange.${property}`);
                        entity[property] = value;
                        onChange && onChange(entity);
                    }
                });

                this.entities[configuration.jsonIdentifier][entity.id] = entity;
            });
        }

        Object.values(this.entities).forEach((type) => Object.values(type).forEach((entity: any) => {
            const configuration: VisualConfiguration = Reflect.getOwnMetadata(Visual, entity.constructor);

            if (deferredReferences.has(entity)) {
                deferredReferences.get(entity)!.forEach(([property, reference]) => {
                    const onChange = safe(configuration, `onChange.${property}`);

                    if (isArray(reference.id)) {
                        entity[property] = reference.id.map((v) => v === null ? v : this.entities[reference.type][v]);
                    } else {
                        entity[property] = this.entities[reference.type][reference.id];
                    }

                    onChange && onChange(entity);
                });
            }

            if (!configuration.show) return;

            const meta: VisualMetadata = {
                marker: new Marker([0, 0], {
                    riseOnHover: true,
                }),
            } as any;

            if (typeof configuration.show.at === 'function') {
                const p = configuration.show.at(entity);
                if (p) {
                    meta.marker.setLatLng(LeafletUtil.latLng(p));
                    this.activeLayers.add(meta.marker);
                }
            } else {
                this.movingEntities.push(entity);
                meta.route = configuration.show.at.route(entity);
                meta.speed = configuration.show.at.speed(entity);
                if (meta.route) {
                    meta.routeIndex = 0;
                    meta.distanceFromLastPoint = 0;
                    meta.distances = Geo.distances(meta.route);
                    if (!configuration.show.when || configuration.show.when(entity)) {
                        meta.marker.setLatLng(LeafletUtil.latLng(meta.route.points[0]));
                        this.activeLayers.add(meta.marker);
                    }
                }
            }

            if (configuration.show.icon) {
                meta.marker.setIcon(configuration.show.icon(entity));
            }

            if (configuration.show.onMarkerEvent) {
                Object.entries(configuration.show.onMarkerEvent).forEach(([event, callback]) => {
                    meta.marker.on(event, (e: any) => (callback as any)(entity, e));
                });
            }

            if (configuration.show.popup) {
                meta.popup = new Popup().setContent(configuration.show.popup(entity));
                meta.marker.bindPopup(meta.popup);
            }

            Reflect.defineMetadata(Visual, meta, entity);
        }));
    }

    applyChange(entity: any, data: EntityChanges, from: 'old' | 'new') {
        const configuration: VisualConfiguration = Reflect.getOwnMetadata(Visual, entity.constructor);

        Object.defineProperty(data, 'id', { enumerable: false });
        Object.keys(data).forEach((name) => {
            let property = data[name][from];

            if (isIdReference(property)) {
                if (isArray(property.id)) {
                    property = property.id.map((id: number) => this.entities[property.type][id] || null);
                } else {
                    property = this.entities[property.type][property.id] || null;
                }
            }

            entity[name] = property;

            const onChange = safe(configuration, `onChange.${name}`);

            onChange && onChange(entity);
        });

        if (!configuration.show) return;

        const meta: VisualMetadata = Reflect.getOwnMetadata(Visual, entity);

        if (typeof configuration.show.at === 'function') {
            const p = configuration.show.at(entity);
            if (p) {
                meta.marker.setLatLng(LeafletUtil.latLng(p));
                this.activeLayers.add(meta.marker);
            } else {
                this.activeLayers.delete(meta.marker);
            }
        } else {
            const route = configuration.show.at.route(entity);
            meta.speed = configuration.show.at.speed(entity);

            if (!isEqual(route, meta.route)) {
                meta.route = route;
                if (meta.route) {
                    meta.distanceFromLastPoint = 0;
                    meta.distances = Geo.distances(meta.route);
                    if (from === 'new') {
                        meta.routeIndex = 0;
                        meta.marker.setLatLng(LeafletUtil.latLng(meta.route.points[0]));
                    } else {
                        meta.routeIndex = meta.route.points.length - 2;
                        meta.marker.setLatLng(LeafletUtil.latLng(meta.route.points[meta.routeIndex + 1]));
                    }
                }
            }

            if (configuration.show.when) {
                if (configuration.show.when(entity)) {
                    this.activeLayers.add(meta.marker);
                } else {
                    this.activeLayers.delete(meta.marker);
                }
            }
        }

        if (configuration.show.icon) {
            meta.marker.setIcon(configuration.show.icon(entity));
        }

        if (configuration.show.popup) {
            meta.popup.setContent(configuration.show.popup(entity));
        }
    }

    is(...states: Array<STATE>): boolean;
    is(...states: Array<[STATE, boolean]>): boolean;
    is(...states: Array<STATE | [STATE, boolean]>): boolean {
        if (isArray(states[0])) {
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
            this.time = -1;
        }

        if (this.is(STATE.END)) {
            this.timeEntryIndex = this.timeEntries.current.length - 1;
            this.time = this.timeEntries.current[this.timeEntryIndex].time;
            this.fromEnd = true;
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

    currentEntry() {
        return this.timeEntries.current[this.timeEntryIndex];
    }

    moveEntitiesForward(span: number) {
        if (span === 0) return;
        this.movingEntities.forEach((entity) => {
            const configuration: VisualConfiguration = Reflect.getOwnMetadata(Visual, entity.constructor);

            if (!configuration.show) return;

            if (!configuration.show.when || configuration.show.when(entity)) {
                const meta: VisualMetadata = Reflect.getOwnMetadata(Visual, entity);

                if (meta.route) {
                    let distanceBetweenCurrentPoints = meta.distances[meta.routeIndex];
                    let distance = span * meta.speed + meta.distanceFromLastPoint;

                    while (distance > distanceBetweenCurrentPoints) {
                        if (meta.routeIndex === meta.distances.length - 1) {
                            distance = distanceBetweenCurrentPoints;
                            break;
                        }

                        distance -= distanceBetweenCurrentPoints;
                        distanceBetweenCurrentPoints = meta.distances[++meta.routeIndex];
                    }

                    const delta = distance / distanceBetweenCurrentPoints;
                    const p1 = meta.route.points[meta.routeIndex];
                    const p2 = meta.route.points[meta.routeIndex + 1];

                    const p = delta === 1 ? p2 : {
                        latitude: p1.latitude + delta * (p2.latitude - p1.latitude),
                        longitude: p1.longitude + delta * (p2.longitude - p1.longitude),
                    };

                    meta.distanceFromLastPoint = distance;
                    meta.marker.setLatLng(LeafletUtil.latLng(p));
                }
            }
        });
    }

    moveEntitiesBackward(span: number) {
        if (span === 0) return;
        this.movingEntities.forEach((entity) => {
            const configuration: VisualConfiguration = Reflect.getOwnMetadata(Visual, entity.constructor);

            if (!configuration.show) return;

            if (!configuration.show.when || configuration.show.when(entity)) {
                const meta: VisualMetadata = Reflect.getOwnMetadata(Visual, entity);

                if (meta.route) {
                    let distanceBetweenCurrentPoints = meta.distances[meta.routeIndex];
                    let distance = span * meta.speed + distanceBetweenCurrentPoints - meta.distanceFromLastPoint;

                    while (distance > distanceBetweenCurrentPoints) {
                        if (meta.routeIndex === 0) {
                            distance = distanceBetweenCurrentPoints;
                            break;
                        }

                        distance -= distanceBetweenCurrentPoints;
                        distanceBetweenCurrentPoints = meta.distances[--meta.routeIndex];
                    }

                    const delta = distance / distanceBetweenCurrentPoints;
                    const p1 = meta.route.points[meta.routeIndex];
                    const p2 = meta.route.points[meta.routeIndex + 1];

                    const p = delta === 1 ? p1 : {
                        latitude: p2.latitude + delta * (p1.latitude - p2.latitude),
                        longitude: p2.longitude + delta * (p1.longitude - p2.longitude),
                    };

                    meta.distanceFromLastPoint = distanceBetweenCurrentPoints - distance;
                    meta.marker.setLatLng(LeafletUtil.latLng(p));
                }
            }
        });
    }

    increaseEntryIndex() {
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
                return false;
            }
        }

        return true;
    }

    decreaseEntryIndex() {
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
                return false;
            }
        }

        return true;
    }

    forwardEntry(timeEntry: HistoryTimeEntry) {
        for (let i = 0; i < timeEntry.events.length; i++) {
            const event = timeEntry.events[i];
            console.log('new', timeEntry.time, event);
            Object.keys(event.changes).forEach((jsonIdentifier) => {
                event.changes[jsonIdentifier].forEach((changes) => {
                    const entity = this.entities[jsonIdentifier][changes.id];
                    this.applyChange(entity, changes, 'new');
                });
            });
        }
    }

    rewindEntry(timeEntry: HistoryTimeEntry) {
        for (let i = timeEntry.events.length - 1; i >= 0; i--) {
            const event = timeEntry.events[i];
            console.log('old', timeEntry.time, event);
            Object.keys(event.changes).forEach((jsonIdentifier) => {
                event.changes[jsonIdentifier].forEach((changes) => {
                    const entity = this.entities[jsonIdentifier][changes.id];
                    this.applyChange(entity, changes, 'old');
                });
            });
        }
    }

    stepForward() {
        if (this.is(STATE.START)) {
            this.updateState(STATE.PAUSED);
            this.time = 0;
        }

        const timeEntry = this.currentEntry();
        this.moveEntitiesForward(timeEntry.time - this.time);
        this.forwardEntry(timeEntry);
        this.time = timeEntry.time;
        this.increaseEntryIndex();
    }

    stepBackward() {
        if (this.is(STATE.END)) {
            this.updateState(STATE.PAUSED);
            this.fromEnd = false;
        } else {
            this.decreaseEntryIndex();
        }

        const timeEntry = this.currentEntry();

        if (this.time > timeEntry.time) {
            this.moveEntitiesBackward(this.time - timeEntry.time);
            this.time = timeEntry.time;
            this.increaseEntryIndex();
            return;
        }

        this.rewindEntry(timeEntry);

        if (!this.decreaseEntryIndex()) return;

        this.time = this.currentEntry().time;
        this.moveEntitiesBackward(timeEntry.time - this.time);
        this.increaseEntryIndex();
    }

    onTick() {
        const  nextTime = (this.time === -1 ? 0 : this.time) + this.speed * this.TICK / 1000;

        let timeEntry: HistoryTimeEntry;

        if (this.speed > 0) {
            timeEntry = this.currentEntry();
            while (timeEntry.time < nextTime) {
                this.moveEntitiesForward(timeEntry.time - this.time);
                this.forwardEntry(timeEntry);
                this.time = timeEntry.time;
                if (!this.increaseEntryIndex()) return;
                timeEntry = this.currentEntry();
            }
            this.moveEntitiesForward(nextTime - this.time);
        } else {
            if (this.fromEnd) {
                this.fromEnd = false;
            } else {
                this.decreaseEntryIndex();
            }
            timeEntry = this.currentEntry();
            while (timeEntry.time > nextTime) {
                this.moveEntitiesBackward(this.time - timeEntry.time);
                this.rewindEntry(timeEntry);
                if (!this.decreaseEntryIndex()) return;
                const lastTime = timeEntry.time;
                timeEntry = this.currentEntry();
                if (timeEntry.time >= nextTime) {
                    this.moveEntitiesBackward(lastTime - timeEntry.time);
                    this.time = timeEntry.time;
                } else {
                    this.time = lastTime;
                }
            }
            this.moveEntitiesBackward(this.time - nextTime);
            this.increaseEntryIndex();
        }

        this.time = nextTime;
    }

    get formattedTime() {
        if (this.time === undefined || this.time < 0) return '--:--:--';
        return moment.unix(this.time).utc().format('HH:mm:ss');
    }
}
