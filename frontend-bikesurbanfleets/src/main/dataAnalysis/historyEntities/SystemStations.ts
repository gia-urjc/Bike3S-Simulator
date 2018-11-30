import { HistoryEntitiesJson } from "../../../shared/history";
import { Station } from "../systemDataTypes/Entities";
import { HistoryReader } from "../HistoryReader";

export class SystemStations {
    private stations: Array<Station>;
	
    public init(history: HistoryReader): void {
        try {
            let entities: HistoryEntitiesJson = history.getEntities("stations");
            this.stations = <Station[]> entities.instances;
        }
        catch(error) {
            throw new Error('Error getting stations: '+error);
        }
        return;
    }
	
    public getStations(): Array<Station> {
	return this.stations;
    }
    
}
