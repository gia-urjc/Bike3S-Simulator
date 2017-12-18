import { BaseEntity } from './BaseEntity';
import { Bike } from './Bike';

export interface BaseStation extends BaseEntity {
    [keyStringStation: string]: any;
    bikes: number | Bike[];
}