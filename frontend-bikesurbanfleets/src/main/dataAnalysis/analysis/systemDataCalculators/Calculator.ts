import { Observable } from '../ObserverPattern';

export interface Calculator extends Observable {
  async calculate(): Promise<void>;
}