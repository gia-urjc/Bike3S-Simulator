import { JsonIdentifier } from './decorators';
import { Entity } from './Entity';

@JsonIdentifier('bikes')
export class Bike extends Entity {
    reserved: boolean;
}
