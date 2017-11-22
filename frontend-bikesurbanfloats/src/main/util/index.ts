import HistoryReader from './HistoryReader';
import IpcUtil from './IpcUtil';

interface AnyObject {
    [key: string]: any,
    [key: number]: any,
}

export {
    AnyObject,
    HistoryReader,
    IpcUtil,
}
