import { Component, Inject } from '@angular/core';
import { marker } from 'leaflet';
import { isArray, without } from 'lodash';
import { IntervalObservable } from 'rxjs/observable/IntervalObservable';
import { AjaxProtocol } from '../../ajax/AjaxProtocol';
import * as entities from './entities';
import { JsonIdentifier, VisualEntity, VisualOptions } from './entities/decorators';
import { Entity } from './entities/Entity';

@Component({
    selector: 'visualization',
    template: require('./visualization.component.html'),
})
export class VisualizationComponent {

    private entities: {
        [key: string]: {
            [key: number]: Entity
        }
    };

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {}

    ngOnInit() {
        this.createEntities().catch((error) => console.error(error));
    }

    getJsonIdentifier(EntityConstructor: Function): string {
        if (!Reflect.hasOwnMetadata(JsonIdentifier, EntityConstructor)) {
            throw new Error(`No json identifier found on ${EntityConstructor.name}`);
        }
        return Reflect.getOwnMetadata(JsonIdentifier, EntityConstructor);
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

    applyChange(entity: any, data: any, from: 'old' | 'new') {
        without(Object.keys(data), 'id').forEach((name) => {
            let property = data[name][from];

            if ('type' in property && 'id' in property) {
                if (isArray(property.id)) {
                    property = property.id.map((id: number) => this.entities[property.type][id] || null);
                } else {
                    property = this.entities[property.type][property.id] || null;
                }
            }

            entity[name] = property;
        });
    }
}
