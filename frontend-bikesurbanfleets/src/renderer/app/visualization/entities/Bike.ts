import { Entity, Historic } from './Entity';

@Historic<Bike>({
    jsonIdentifier: 'bikes',
})
export class Bike extends Entity {
    reserved: boolean;
}
