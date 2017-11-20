export interface AjaxHistory {
    init: (path: string) => Promise<void>,
    entities: () => Promise<object>,
    nChanges: () => Promise<number>,
    previousChange: () => Promise<object>,
    nextChange: () => Promise<object>,
}

export interface AjaxProtocol {
    history: AjaxHistory
}
