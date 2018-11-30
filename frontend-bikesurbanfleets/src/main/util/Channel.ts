export default class Channel {
    
    constructor(public name: string, public callback: (data?: any) => Promise<any>) {}
    
}