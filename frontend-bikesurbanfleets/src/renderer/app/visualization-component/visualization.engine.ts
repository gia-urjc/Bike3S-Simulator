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
    private prototypes: {
        [key: string]: {
        }
    };

    //two members that reflect the references between Entities
    // entries for <string EntityJsonIdentifier+id> referenced as <String property> by another <string EntityJsonIdentifier+id> in the 
    // object Entity
    // that is the entity keys are used as <string EntityJsonIdentifier+id>
    private referencedEntities: Map<string, Map<string, Map<string,Entity>>>;
    // entries for Entity that include a reference as <String property> to another Entity 
    private entityReferences: Map<string, Map<string, Set<string>>>;

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
        this.entities={};
        this.prototypes={};
        this.entityReferences = new Map();
        this.activeLayers.clear();
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
        if (reference === null || host===null) return;
        //getting the stringids
        const referenceID = `${Reflect.getOwnMetadata(Historic, reference.constructor).jsonIdentifier}${reference.id}`;
        const hostID = `${Reflect.getOwnMetadata(Historic, host.constructor).jsonIdentifier}${host.id}`;

        //add hosts to referenced object
        const referenceMap = this.referencedEntities.get(referenceID) || new Map<string, Map<string,Entity>>();
        const hostMap = referenceMap.get(property) || new Map<string,Entity>();

        hostMap.set(hostID,host);
        referenceMap.set(property, hostMap);
        this.referencedEntities.set(referenceID, referenceMap);
        
        //add referenced object to host
        const hostMap2 = this.entityReferences.get(hostID) || new Map<string, Set<string>>();
        const propertyMap = hostMap2.get(property) || new Set<string>();

        propertyMap.add(referenceID);
        hostMap2.set(property, propertyMap);
        this.entityReferences.set(hostID, hostMap2);
    }

    private deRegisterReferencesForHost(host: Entity | null) {
        if (host === null) return;
        const hostID = `${Reflect.getOwnMetadata(Historic, host.constructor).jsonIdentifier}${host.id}`;
        const hostMap = this.entityReferences.get(hostID);
        if (hostMap) {
            //delete entries in referencedEntities
            hostMap.forEach((refset: Set<string>, property: string) => {
                refset.forEach((referenceID : string) => {
                    const referenceMap = this.referencedEntities.get(referenceID);
                    const hostMap2 = referenceMap.get(property);
                    hostMap2.delete(hostID);
                    if (hostMap2.size===0) {
                        referenceMap.delete(property);
                        if(referenceMap.size===0) {
                            this.referencedEntities.delete(referenceID);
                        }
                    }
                });
            });
            //delete entries in entityReferences
            this.entityReferences.delete(hostID);
        }
    }
   
    private deRegisterReferencesForProperty(host: Entity, property: string) {
        const hostID = `${Reflect.getOwnMetadata(Historic, host.constructor).jsonIdentifier}${host.id}`;
        const hostMap = this.entityReferences.get(hostID);
        if (hostMap) {
            //delete entries in referencedEntities
            const refset = hostMap.get(property);
            if(refset) {
                refset.forEach((referenceID : string) => {
                    const referenceMap = this.referencedEntities.get(referenceID);
                    const hostMap2 = referenceMap.get(property);
                    hostMap2.delete(hostID);
                    if (hostMap2.size===0) {
                        referenceMap.delete(property);
                        if(referenceMap.size===0) {
                            this.referencedEntities.delete(referenceID);
                        }
                    }
                });
              //delete entries in entityReferences
               hostMap.delete(property);
            }
            if (hostMap.size===0) {
                this.entityReferences.delete(hostID);
            }
        }
    }

    private async load(historyPath: string): Promise<void> {

        this.movingEntities = [];
        this.referencedEntities = new Map();
        this.entities={};
        this.prototypes={};
        this.entityReferences = new Map();
        this.activeLayers.clear();

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
    //    console.log(`${STATE[this.state]} -> ${STATE[state]}`);

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
        var newEntities: {
        [key: string]: {
            [key: number]: Entity
            }
        };
        newEntities={};
        const deferredReferences = new Map<Entity, Array<[string, IdReference]>>();

        this.entities = {};
        this.prototypes={};

        for (let Constructor of Object.values(EntityConstructors)) {
            const configuration: HistoricConfiguration = Reflect.getOwnMetadata(Historic, Constructor);
            const entitySource = await this.ajax.history.getEntities(configuration.jsonIdentifier);

            this.entities[configuration.jsonIdentifier] = {};
            newEntities[configuration.jsonIdentifier]={};
 
            this.prototypes[configuration.jsonIdentifier]=entitySource.prototype;

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
                newEntities[configuration.jsonIdentifier][entity.id] = entity;
            });
        }
        this.includeNewEntities(newEntities,deferredReferences);
    }
    
    includeNewEntities(newEntities: any, deferredReferences: Map<Entity, Array<[string, IdReference]>>) {

        Object.values(newEntities).forEach((type) => Object.values(type).forEach((entity: any) => {
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
    
    treatNewEntities(newentitiesJson ) {
        if (newentitiesJson===null) return;
        var newEntities: {
        [key: string]: {
            [key: number]: Entity
            }
        };
        newEntities={};
        const deferredReferences = new Map<Entity, Array<[string, IdReference]>>();

         //create the entities       
        Object.keys(newentitiesJson).forEach((entityIdentifier) => {
            
            newEntities[entityIdentifier]={};
            //find the constructor
            
            var entityconstructor;
            for (let Constructor of Object.values(EntityConstructors)) {
                if (Reflect.getOwnMetadata(Historic, Constructor).jsonIdentifier===entityIdentifier){
                    entityconstructor=Constructor;
                    break;
                }
            }
 
            newentitiesJson[entityIdentifier].forEach((jsonEntity) => { //for ech entity entry
                 
                const entity = Reflect.construct(entityconstructor, []);

                this.prototypes[entityIdentifier].forEach((property) => entity[property] = null);

                Object.entries(jsonEntity).forEach(([property, value]) => {
                    if (isIdReference(value)) {
                        const references = deferredReferences.get(entity) || [];
                        references.push([property, value]);
                        deferredReferences.set(entity, references);
                    } else {
                        entity[property] = value;
                    }
                });

                newEntities[entityIdentifier][entity.id] = entity;
                //add new entity to entities
                if (!this.entities[entityIdentifier]){
                    this.entities[entityIdentifier] = {};
                }
                this.entities[entityIdentifier][entity.id] = entity;
            });
        });
        this.includeNewEntities(newEntities,deferredReferences);
    }
    
    treatOldEntities(oldentitiesJson ) {
        if (oldentitiesJson===null) return;
        
         //create the entities       
        Object.keys(oldentitiesJson).forEach((entityIdentifier) => {
            
             oldentitiesJson[entityIdentifier].forEach((jsonEntity) => { //for ech entity entry
                 
                const entity=this.entities[entityIdentifier][jsonEntity.id];
                //call teh delete of the object
                const configuration: HistoricConfiguration = Reflect.getOwnMetadata(Historic, entity.constructor);
                const onDelete = safe(configuration, 'on.delete');
                onDelete && onDelete(entity);
                //delete the doisplaymarker
                const displayData: DisplayData = Reflect.getOwnMetadata(DisplayKey, entity);
                if (displayData) this.activeLayers.delete(displayData.marker);
                //delete references
                this.deRegisterReferencesForHost(entity);
                //delete object
                delete this.entities[entityIdentifier][jsonEntity.id];
             });
        });
    }

    treatChanges(entitychanges, from: 'old' | 'new') {
        if (entitychanges===null) return;
    
        Object.keys(entitychanges).forEach((jsonIdentifier) => {
            entitychanges[jsonIdentifier].forEach((data) => {
                const entity = this.entities[jsonIdentifier][data.id];    
                const configuration: HistoricConfiguration = Reflect.getOwnMetadata(Historic, entity.constructor);
                Object.defineProperty(data, 'id', { enumerable: false });
                //update all properties
                Object.keys(data).forEach((name) => {
                    let property = data[name][from];
                    //treat changes of references
                    if (isIdReference(property)) {
                        //first delete old references
                        this.deRegisterReferencesForProperty(entity,property.type);
                        //second set new references
                        if (isArray(property.id)) {
                            property = property.id.map((id: number) => this.entities[property.type][id] || null);
                            property.forEach((reference: Entity | null) => this.registerReference(reference, entity, name));
                        } else {
                            property = this.entities[property.type][property.id] || null;
                            this.registerReference(property, entity, name);
                        }
                    }                    
                    entity[name] = property;
                    //call the propertyupdate of the entity
                    const onProperty = safe(configuration, `on.propertyUpdate.${name}`);
                    onProperty && onProperty(entity);
                });
                //call the update of the entity
                const onUpdate = safe(configuration, 'on.update');
                onUpdate && onUpdate(entity);
                // call reference updates if necessary
                const referenceID = `${configuration.jsonIdentifier}${entity.id}`;
                const referenceMap = this.referencedEntities.get(referenceID);
                if (referenceMap) {
                    referenceMap.forEach((hostMap, property) => {
                        hostMap.forEach((host: Entity, hostid: string) => {
                            const hostConfiguration: HistoricConfiguration = Reflect.getOwnMetadata(Historic, host.constructor);
                            const onRefUpdate = safe(hostConfiguration, `on.referenceUpdate.${property}`);
                            onRefUpdate && onRefUpdate(host, entity);
                        });
                    });
                }
                //update the displaydata
                this.updateMarkerDisplay(configuration, entity, from);
            });
        });
    }
   
    updateMarkerDisplay(configuration: any, entity: any,from: 'old' | 'new'){
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

                    const delta = distanceBetweenCurrentPoints === 0 ? 1 : distance / distanceBetweenCurrentPoints;
                    const p1 = displayData.route.points[displayData.routeIndex];
                    const p2 = displayData.route.points[displayData.routeIndex + 1];

                    const p =  delta === 1 ? p2 : {
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

                    const delta = distanceBetweenCurrentPoints === 0 ? 1 : distance / distanceBetweenCurrentPoints;
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
            if (event.newEntities) {
                this.treatNewEntities(event.newEntities);
            }
            if (event.changes) {
                this.treatChanges(event.changes,'new');
            }
            if (event.oldEntities) {
                this.treatOldEntities(event.oldEntities);
            }
        }
    }

    rewindEntry(timeEntry: HistoryTimeEntry) {
        for (let i = timeEntry.events.length - 1; i >= 0; i--) {
            const event = timeEntry.events[i];
     //       console.log('old', timeEntry.time, event);
            if (event.oldEntities) {
                this.treatNewEntities(event.oldEntities);
            }
            if (event.changes) {
                this.treatChanges(event.changes,'old');
            } 
            if (event.newEntities) {
                this.treatOldEntities(event.newEntities);
            }
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
