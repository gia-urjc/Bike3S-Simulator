import { Component, Inject } from '@angular/core';
import { isArray, without } from 'lodash';
import { AjaxProtocol } from '../../ajax/AjaxProtocol';
import * as entities from './entities';
import { Entity, EntityMetaKey, VisualOptions } from './entities/Entity';

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

    getMetadata(EntityConstructor: Function): VisualOptions {
        if (!Reflect.hasOwnMetadata(EntityMetaKey, EntityConstructor)) {
            throw new Error(`No metadata found on ${EntityConstructor}`);
        }

        return Reflect.getOwnMetadata(EntityMetaKey, EntityConstructor);
    }

    async createEntities(): Promise<void> {
        await this.ajax.history.init('history');
        const entitySource: { [key: string]: Array<any> } = await this.ajax.history.readEntities() as any;

        this.entities = {};

        Object.values(entities).forEach((EntityConstructor) => {
            const meta = this.getMetadata(EntityConstructor);

            if (!(meta.fromJson in entitySource)) return;

            this.entities[meta.fromJson] = {};

            entitySource[meta.fromJson].forEach((json) => {
                const entity: Entity = Reflect.construct(EntityConstructor, [json]);
                this.entities[meta.fromJson][entity.id] = entity;
            });
        });

        console.log(this.entities);
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
