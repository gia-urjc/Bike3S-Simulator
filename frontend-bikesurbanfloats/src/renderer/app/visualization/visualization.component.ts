import { Component, Inject } from '@angular/core';
import { JsonObject } from '../../../shared/util';
import { AjaxProtocol } from '../../ajax/AjaxProtocol';
import * as entities from './entities';
import { Entity, EntityMetaKey, VisualOptions } from './entities/Entity';

@Component({
    selector: 'visualization',
    template: require('./visualization.component.html'),
})
export class VisualizationComponent {

    static testList: Array<any> = [];

    private entities: Map<Function, { [key: number]: Entity }>;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {}

    ngOnInit() {
        this.createEntities().catch((error) => console.error(error));
    }

    async createEntities(): Promise<void> {
        await this.ajax.history.init('history');
        const entitySource = await this.ajax.history.readEntities();

        this.entities = new Map();

        Object.values(entities).forEach((EntityCostructor) => {
            if (!Reflect.hasOwnMetadata(EntityMetaKey, EntityCostructor)) {
                console.warn(`No metadata found on ${EntityCostructor}`);
                return;
            }

            const meta: VisualOptions = Reflect.getOwnMetadata(EntityMetaKey, EntityCostructor);

            if (!(meta.fromJson in entitySource)) return;

            this.entities.set(EntityCostructor, {});

            (entitySource as any)[meta.fromJson].forEach((json: JsonObject) => {
                const entity: Entity = Reflect.construct(EntityCostructor, [json]);
                this.entities.get(EntityCostructor)![entity.id] = entity;
            });
        });
    }
}
