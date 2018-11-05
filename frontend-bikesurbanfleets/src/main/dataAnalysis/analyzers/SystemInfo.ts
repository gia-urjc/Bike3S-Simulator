import { Data } from "./Data";

export interface SystemInfo {
    basicData: any;
    data: Data;
    getData(): Data;
    init(): void;
}