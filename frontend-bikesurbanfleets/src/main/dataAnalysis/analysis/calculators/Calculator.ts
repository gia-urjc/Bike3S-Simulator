import { Observable } from '../ObserverPattern';

export interface Calculator extends Observable {
 calculate(): Promise<void>;
}