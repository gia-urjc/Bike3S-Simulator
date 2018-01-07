import { Entity, Visual } from './Entity';

@Visual<Bike>({
    jsonIdentifier: 'bikes',
})
export class Bike extends Entity {
    reserved: boolean;
}
