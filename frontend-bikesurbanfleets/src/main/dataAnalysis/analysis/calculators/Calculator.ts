import { Observable } from '../ObserverPattern';

export interface Calculator extends Observable {
 calculate(path?: string): Promise<void>;
}