export interface Observer {
    update(data: any): void;
}

export interface Observable {
    notify(data: any): void;
    subscribe(observer: Observer): void;
}