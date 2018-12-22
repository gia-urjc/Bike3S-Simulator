import { HistoryEntity } from "../../../shared/history";

export default interface HistoryEntityStation extends HistoryEntity {
    capacity: number;
    availablebikes: number;
    reservedbikes: number;
    reservedslots: number;
    availableslots: number;
}