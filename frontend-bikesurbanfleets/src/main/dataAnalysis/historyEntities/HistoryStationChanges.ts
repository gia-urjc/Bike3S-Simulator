import { HistoryEntity } from "../../../shared/history";

export default interface HistoryStationChanges extends HistoryEntity {
    capacity: number;
    availablebikes: {
        old: number,
        new: number
    };
    reservedbikes: {
        old: number;
        new: number;
    };
    reservedslots: {
        old: number,
        new: number
    };
    availableslots: {
        old: number,
        new: number
    };
}
