export interface AjaxHistory {
    init(path: string): Promise<void>,
    readEntities(): Promise<object>,
    numberOFChangeFiles(): Promise<number>,
    previousChangeFile(): Promise<object>,
    nextChangeFile(): Promise<object>,
}

export interface AjaxProtocol {
    history: AjaxHistory
}
