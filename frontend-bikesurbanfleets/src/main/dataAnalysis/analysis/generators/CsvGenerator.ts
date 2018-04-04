import { HistoryEntitiesJson } from "../../../../shared/history";
import { JsonObject } from "../../../../shared/util";
import { HistoryReader } from "../../../util";
import { Station, User, Entity } from "../../systemDataTypes/Entities";
import { SystemGlobalInfo } from "../SystemGlobalInfo";
import { AbsoluteValue } from "../absoluteValues/AbsoluteValue";
import { SystemInfo } from "../absoluteValues/SystemInfo";
import { RentalAndReturnAbsoluteValue } from "../absoluteValues/rentalsAndReturns/RentalAndReturnAbsoluteValue";
import { RentalAndReturnData } from "../absoluteValues/rentalsAndReturns/RentalAndReturnData";
import { RentalsAndReturnsPerStation } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationAbsoluteValue } from "../absoluteValues/reservations/ReservationAbsoluteValue";
import { ReservationData } from "../absoluteValues/reservations/ReservationData";
import { ReservationsPerStation } from "../absoluteValues/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../absoluteValues/reservations/ReservationsPerUser";
import * as json2csv from 'json2csv';
import * as fs from 'fs';

export class CsvGenerator {
    private titles: Array<string>;
   	private stationData: Array<JsonObject>;
   	private userData: Array<JsonObject>;
    private historyPath: string;
    private csvPath: string;
    private schemaPath: string | undefined;
    private globalValuesFields: Array<string>;
    private globalValues: JsonObject;

    public constructor(historyPath: string, globalValues: SystemGlobalInfo, csvPath: string, schemaPath?: string) {
    this.titles = new Array();
    this.stationData = new Array();
    this.userData = new Array();
    this.globalValuesFields = new Array();
    this.globalValues = {};
    this.globalValues.demand_satisfaction = globalValues.getDemandSatisfaction();
    this.globalValues.hire_efficiency = globalValues.getRentalEfficiency();
    this.globalValues.return_efficiency = globalValues.getReturnEfficiency();
    this.historyPath = historyPath;
    this.schemaPath = schemaPath == undefined ? undefined : schemaPath;
    this.csvPath = csvPath;
  }
    
	 public async generate(info: Map<string, SystemInfo>): Promise<void> {
      try {
          await this.init(info);
          this.transformToCsv();
      }
      catch(error) {
          throw new Error('Error initializing csv generator: '+error);
      }
      return;
	 }
     
  public createJsonFor(entities: Array<Entity>, data: Array<JsonObject>, reservations: SystemInfo, rentalsAndReturns: SystemInfo): void {
    let i: number = 1;  // title index
    let j: number = 0;  // data index
      
    for (let entity of entities) {
        let jsonObj: JsonObject = {};
        
        jsonObj.id = entity.id;
        
        while (j < 4) {   // adding reservations' data
            let reservationValues: AbsoluteValue | undefined = reservations.getData().absoluteValues.get(entity.id);
            if (reservationValues !== undefined) {
                let absValueList: Array<number>= reservationValues.getAbsoluteValuesAsArray();
                jsonObj[this.titles[i]] = absValueList[j]; 
                i++;
                j++;
            }
        }
        
        j = 0;
        while (j < 4) {   // adding rentals and returns' data
            let rentalAndReturnValues: AbsoluteValue | undefined = rentalsAndReturns.getData().absoluteValues.get(entity.id);
            if (rentalAndReturnValues !== undefined) {
                let absValueList: Array<number> = rentalAndReturnValues.getAbsoluteValuesAsArray();
                jsonObj[this.titles[i]] = absValueList[j];
                i++;   
                j++;
            }
        }
        data.push(jsonObj);
    }
  } 

	 public async init(info: Map<string, SystemInfo>): Promise<void> {
      this.titles.push('id');
      ReservationData.NAMES.forEach( (name) => this.titles.push(name));
      RentalAndReturnData.NAMES.forEach( (name) => this.titles.push(name));
          
      let history: HistoryReader = await HistoryReader.create(this.historyPath);
         
      let historyStations: HistoryEntitiesJson = await history.getEntities('stations');    
      let stations: Array<Station> = <Station[]> historyStations.instances;
         
      let reservations: SystemInfo | undefined = info.get(ReservationsPerStation.name);
      let rentalsAndReturns: SystemInfo | undefined = info.get(RentalsAndReturnsPerStation.name);
      if (reservations && rentalsAndReturns) {
        this.createJsonFor(stations, this.stationData, reservations, rentalsAndReturns);
      }
         
      let historyUsers: HistoryEntitiesJson = await history.getEntities('users');
      let users: Array<User> = <User[]> historyUsers.instances;
             
      reservations = info.get(ReservationsPerUser.name);
      rentalsAndReturns = info.get(RentalsAndReturnsPerUser.name);
      if (reservations && rentalsAndReturns) {   
        this.createJsonFor(users, this.userData, reservations, rentalsAndReturns);
      }
	}

	public transformToCsv(): void {
        let csv = json2csv({ data: this.stationData, fields: this.titles, withBOM: true });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/stations.csv`, csv, (err) => {
          if (err) throw err;
          console.log('stations file saved');
        });
        csv = json2csv({ data: this.userData, fields: this.titles, withBOM: true });
        fs.writeFile (`${this.csvPath}/users.csv`, csv, (err) => {
          if (err) throw err;
          console.log('user file saved');
        });
        csv = json2csv({data: [this.globalValues], fields: this.globalValuesFields, withBOM: true });
        fs.writeFile(`${this.csvPath}/global_values.csv`, csv, (err) => {
            if (err) throw err;
            console.log('global values file saved');
        });
	}

	private checkFolders(): void {
        if(this.csvPath !== undefined && !fs.existsSync(this.csvPath)) {
            fs.mkdirSync(this.csvPath);
        }
    }

}  