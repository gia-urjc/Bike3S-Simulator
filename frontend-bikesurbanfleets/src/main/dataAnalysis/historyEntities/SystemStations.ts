import { HistoryEntitiesJson } from "../../../shared/history";
import { HistoryReaderController } from "../../controllers/HistoryReaderController";
import { Station } from "../systemDataTypes/Entities";

export class SystemStations {
    private stations: Array<Station>;
	
    public async init(history: HistoryReaderController): Promise<void> {
        try {
            let entities: HistoryEntitiesJson = await history.getEntities("stations");
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
