import { Data } from "./Data";

export interface SystemInfo {
    data: Data;
    getData(): Data;
    init(): void;
}