import { HistoryEntitiesJson } from "../../../shared/history";
import { HistoryReader } from "../HistoryReader";
import HistoryEntityStation from "../historyEntities/HistoryEntityStation";

export class SystemStations {
    private stations: Array<HistoryEntityStation>;
	
    public init(history: HistoryReader): void {
        try {
            let entities: HistoryEntitiesJson = history.getEntities("stations");
            this.stations = <HistoryEntityStation[]> entities.instances;
        }
        catch(error) {
            throw new Error('Error getting stations: '+error);
        }
        return;
    }
	
    public getStations(): Array<HistoryEntityStation> {
	    return this.stations;
    }
    
}
