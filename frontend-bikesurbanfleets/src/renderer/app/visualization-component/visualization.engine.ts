import { FormControl, Validators } from '@angular/forms';
import { Layer, Marker, Popup } from 'leaflet';
import { isArray, isEqual } from 'lodash';

import { IntervalObservable } from 'rxjs/observable/IntervalObservable';
import { takeWhile } from 'rxjs/operators/takeWhile';

import { EntityChanges, HistoryTimeEntry, IdReference, isIdReference } from '../../../shared/history';
import { Geo, safe } from '../../../shared/util';
import { AjaxProtocol } from '../../ajax/AjaxProtocol';
import { Entity, Historic, HistoricConfiguration } from './entities/Entity';
import { breakPoint, LeafletUtil } from './util';

import * as EntityConstructors from './entities';

interface DisplayData {
    marker: Marker;
    route: Geo.Route | null;
    speed: number;
    routeIndex: number;
    distances: Array<number>;
    distanceFromLastPoint: number;
    popup: Popup;
}

const DisplayKey = Symbol('DisplayData');

export enum STATE {
    LOADING = 1,

    START = 2,
    PAUSED = 4,
    END = 8,

    FORWARD = 16,
    REWIND = 32,

    NOT_RUNNING = START | PAUSED | END, // don't include the loading state
    RUNNING = FORWARD | REWIND,
}

export class VisualizationEngine {

    readonly NO_EMIT = { emitEvent: false };

    mainActionIcon: string;
    speedControl: FormControl;
    state: STATE;
    fromEnd: boolean;

    speed: number;
    time: number;
    timeEntryIndex: number;
    nChangeFiles: number;
    changeFileIndex: number;
    movingEntities: Array<Entity>;

    private entities: {
        [key: string]: {
            [key: number]: Entity
        }
    };

    private referencedEntities: Map<Entity, Map<string, Set<Entity>>>;

    private timeEntries: {
        previous: Array<HistoryTimeEntry> | null,
        current: Array<HistoryTimeEntry>,
        next: Array<HistoryTimeEntry> | null,
    };

    constructor(
        private ajax: AjaxProtocol,
        private activeLayers: Set<Layer>,
        private refreshRate: number,
    ) {
        this.movingEntities = [];
        this.referencedEntities = new Map();
        this.fromEnd = false;
        this.speed = 10;

        this.speedControl = new FormControl(this.speed, [
            Validators.required,
            Validators.pattern(/-?[1-9][0-9]*/)
        ]);

        this.speedControl.valueChanges.subscribe((value) => {
            if (this.speedControl.errors) return;
            this.speed = parseInt(value);
        });

        this.updateState(STATE.LOADING);
    }

    init(historyPath: string) {
        this.updateState(STATE.LOADING);
        this.load(historyPath)
            .then(() => this.updateState(STATE.START))
            .catch((error) => console.log(error));
    }

    private registerReference(reference: Entity | null, host: Entity, property: string) {
        if (reference === null) return;

        const referenceMap = this.referencedEntities.get(reference) || new Map<string, Set<Entity>>();
        const hostSet = referenceMap.get(property) || new Set<Entity>();

        hostSet.add(host);
        referenceMap.set(property, hostSet);
        this.referencedEntities.set(reference, referenceMap);
    }

    private async load(historyPath: string): Promise<void> {

        this.movingEntities = [];
        this.referencedEntities = new Map();
        this.fromEnd = false;
        this.speed = 10;

        await this.ajax.history.init(historyPath);
        await this.createEntities();

        this.nChangeFiles = await this.ajax.history.numberOFChangeFiles();

        this.changeFileIndex = 0;

        this.timeEntries = {
            previous: null,
            current: await this.ajax.history.getChangeFile(this.changeFileIndex),
            next: this.nChangeFiles > 1 ? await this.ajax.history.getChangeFile(this.changeFileIndex + 1) : null,
        };
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
                .create(this.refreshRate)
                .pipe(takeWhile(() => this.state === STATE.FORWARD))
                .subscribe(() => this.onTick());
        }

        if (this.is(STATE.REWIND)) {
            IntervalObservable
                .create(this.refreshRate)
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

    async createEntities(): Promise<void> {
        const deferredReferences = new Map<Entity, Array<[string, IdReference]>>();

        this.entities = {};

        for (let Constructor of Object.values(EntityConstructors)) {
            const configuration: HistoricConfiguration = Reflect.getOwnMetadata(Historic, Constructor);
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
                        entity[property] = value;
                    }
                });

                this.entities[configuration.jsonIdentifier][entity.id] = entity;
            });
        }

        Object.values(this.entities).forEach((type) => Object.values(type).forEach((entity: any) => {
            const configuration: HistoricConfiguration = Reflect.getOwnMetadata(Historic, entity.constructor);

            if (deferredReferences.has(entity)) {
                deferredReferences.get(entity)!.forEach(([property, reference]) => {
                    if (isArray(reference.id)) {
                        entity[property] = reference.id.map((v) => v === null ? v : this.entities[reference.type][v]);
                        entity[property].forEach((ref: Entity | null) => this.registerReference(ref, entity, property));
                    } else {
                        entity[property] = this.entities[reference.type][reference.id];
                        this.registerReference(entity[property], entity, property);
                    }
                });
            }

            const onInit = safe(configuration, 'on.init');

            onInit && onInit(entity);

            if (!configuration.marker) return;

            const displayData: DisplayData = {
                marker: new Marker([0, 0], {
                    riseOnHover: true,
                }),
            } as any;

            if (typeof configuration.marker.at === 'function') {
                const p = configuration.marker.at(entity);
                if (p) {
                    displayData.marker.setLatLng(LeafletUtil.latLng(p));
                    this.activeLayers.add(displayData.marker);
                }
            } else {
                this.movingEntities.push(entity);
                displayData.route = configuration.marker.at.route(entity);
                displayData.speed = configuration.marker.at.speed(entity);
                if (displayData.route) {
                    displayData.routeIndex = 0;
                    displayData.distanceFromLastPoint = 0;
                    displayData.distances = Geo.distances(displayData.route);
                    if (!configuration.marker.when || configuration.marker.when(entity)) {
                        displayData.marker.setLatLng(LeafletUtil.latLng(displayData.route.points[0]));
                        this.activeLayers.add(displayData.marker);
                    }
                }
            }

            if (configuration.marker.icon) {
                displayData.marker.setIcon(configuration.marker.icon(entity));
            }

            if (configuration.marker.on) {
                Object.entries(configuration.marker.on).forEach(([event, callback]) => {
                    displayData.marker.on(event, (e: any) => (callback as any)(entity, e));
                });
            }

            if (configuration.marker.popup) {
                displayData.popup = new Popup().setContent(configuration.marker.popup(entity));
                displayData.marker.bindPopup(displayData.popup);
            }

            Reflect.defineMetadata(DisplayKey, displayData, entity);
        }));
    }

    applyChange(entity: any, data: EntityChanges, from: 'old' | 'new') {
        const configuration: HistoricConfiguration = Reflect.getOwnMetadata(Historic, entity.constructor);

        Object.defineProperty(data, 'id', { enumerable: false });
        Object.keys(data).forEach((name) => {
            let property = data[name][from];

            if (isIdReference(property)) {
                if (isArray(property.id)) {
                    property = property.id.map((id: number) => this.entities[property.type][id] || null);
                    property.forEach((reference: Entity | null) => this.registerReference(reference, entity, name));
                } else {
                    property = this.entities[property.type][property.id] || null;
                    this.registerReference(property, entity, name);
                }
            }

            entity[name] = property;

            const onProperty = safe(configuration, `on.propertyUpdate.${name}`);

            onProperty && onProperty(entity);
        });

        const onUpdate = safe(configuration, 'on.update');

        onUpdate && onUpdate(entity);

        const referenceMap = this.referencedEntities.get(entity);

        if (referenceMap) {
            referenceMap.forEach((hostSet, property) => {
                hostSet.forEach((host) => {
                    const hostConfiguration: HistoricConfiguration = Reflect.getOwnMetadata(Historic, host.constructor);
                    const onRefUpdate = safe(hostConfiguration, `on.referenceUpdate.${property}`);
                    onRefUpdate && onRefUpdate(host, entity);
                });
            });
        }

        if (!configuration.marker) return;

        const displayData: DisplayData = Reflect.getOwnMetadata(DisplayKey, entity);

        if (typeof configuration.marker.at === 'function') {
            const p = configuration.marker.at(entity);
            if (p) {
                displayData.marker.setLatLng(LeafletUtil.latLng(p));
                this.activeLayers.add(displayData.marker);
            } else {
                this.activeLayers.delete(displayData.marker);
            }
        } else {
            const route = configuration.marker.at.route(entity);
            displayData.speed = configuration.marker.at.speed(entity);

            if (!isEqual(route, displayData.route)) {
                displayData.route = route;
                if (displayData.route) {
                    displayData.distances = Geo.distances(displayData.route);
                    if (from === 'new') {
                        displayData.routeIndex = 0;
                        displayData.distanceFromLastPoint = 0;
                        displayData.marker.setLatLng(LeafletUtil.latLng(displayData.route.points[0]));
                    } else {
                        displayData.routeIndex = displayData.route.points.length - 2;
                        displayData.distanceFromLastPoint = displayData.distances[displayData.routeIndex];
                        displayData.marker.setLatLng(LeafletUtil.latLng(displayData.route.points[displayData.routeIndex + 1]));
                    }
                }
            }

            if (configuration.marker.when) {
                if (configuration.marker.when(entity)) {
                    this.activeLayers.add(displayData.marker);
                } else {
                    this.activeLayers.delete(displayData.marker);
                }
            }
        }

        if (configuration.marker.icon) {
            const icon = configuration.marker.icon(entity);
            if (icon !== displayData.marker.options.icon) displayData.marker.setIcon(icon);
        }

        if (configuration.marker.popup) {
            displayData.popup.setContent(configuration.marker.popup(entity));
        }
    }

    currentEntry() {
        return this.timeEntries.current[this.timeEntryIndex];
    }

    moveEntitiesForward(span: number) {
        if (span === 0) return;
        this.movingEntities.forEach((entity) => {
            const configuration: HistoricConfiguration = Reflect.getOwnMetadata(Historic, entity.constructor);

            if (!configuration.marker) return;

            if (!configuration.marker.when || configuration.marker.when(entity)) {
                const displayData: DisplayData = Reflect.getOwnMetadata(DisplayKey, entity);

                if (displayData.route) {
                    let distanceBetweenCurrentPoints = displayData.distances[displayData.routeIndex];
                    let distance = span * displayData.speed + displayData.distanceFromLastPoint;

                    while (distance > distanceBetweenCurrentPoints) {
                        if (displayData.routeIndex === displayData.distances.length - 1) {
                            distance = distanceBetweenCurrentPoints;
                            break;
                        }

                        distance -= distanceBetweenCurrentPoints;
                        distanceBetweenCurrentPoints = displayData.distances[++displayData.routeIndex];
                    }

                    const delta = distance / distanceBetweenCurrentPoints;
                    const p1 = displayData.route.points[displayData.routeIndex];
                    const p2 = displayData.route.points[displayData.routeIndex + 1];

                    const p = delta === 1 ? p2 : {
                        latitude: p1.latitude + delta * (p2.latitude - p1.latitude),
                        longitude: p1.longitude + delta * (p2.longitude - p1.longitude),
                    };

                    displayData.distanceFromLastPoint = distance;
                    displayData.marker.setLatLng(LeafletUtil.latLng(p));
                }
            }
        });
    }

    moveEntitiesBackward(span: number) {
        if (span === 0) return;
        this.movingEntities.forEach((entity) => {
            const configuration: HistoricConfiguration = Reflect.getOwnMetadata(Historic, entity.constructor);

            if (!configuration.marker) return;

            if (!configuration.marker.when || configuration.marker.when(entity)) {
                const displayData: DisplayData = Reflect.getOwnMetadata(DisplayKey, entity);

                if (displayData.route) {
                    let distanceBetweenCurrentPoints = displayData.distances[displayData.routeIndex];
                    let distance = span * displayData.speed + distanceBetweenCurrentPoints - displayData.distanceFromLastPoint;

                    while (distance > distanceBetweenCurrentPoints) {
                        if (displayData.routeIndex === 0) {
                            distance = distanceBetweenCurrentPoints;
                            break;
                        }

                        distance -= distanceBetweenCurrentPoints;
                        distanceBetweenCurrentPoints = displayData.distances[--displayData.routeIndex];
                    }

                    const delta = distance / distanceBetweenCurrentPoints;
                    const p1 = displayData.route.points[displayData.routeIndex];
                    const p2 = displayData.route.points[displayData.routeIndex + 1];

                    const p = delta === 1 ? p1 : {
                        latitude: p2.latitude + delta * (p1.latitude - p2.latitude),
                        longitude: p2.longitude + delta * (p1.longitude - p2.longitude),
                    };

                    displayData.distanceFromLastPoint = distanceBetweenCurrentPoints - distance;
                    displayData.marker.setLatLng(LeafletUtil.latLng(p));
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
                    this.ajax.history
                        .getChangeFile(this.changeFileIndex + 1)
                        .then((entry) => this.timeEntries.next = entry);
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
        const  nextTime = (this.time === -1 ? 0 : this.time) + this.speed * this.refreshRate / 1000;

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
}
