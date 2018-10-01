import { JsonObject } from "../../../../shared/util";
import { Station, User, Entity } from "../../systemDataTypes/Entities";
import { SystemGlobalInfo } from "../SystemGlobalInfo";
import { AbsoluteValue } from "../absoluteValues/AbsoluteValue";
import { SystemInfo } from "../absoluteValues/SystemInfo";
import { StationBalancingQuality, StationBalancingData } from '../absoluteValues/stations/StationBalancingQuality';
import { BikesPerTime, BikesPerStationAndTime } from '../absoluteValues/stations/BikesPerStationAndTime';
import { RentalAndReturnAbsoluteValue } from "../absoluteValues/rentalsAndReturns/RentalAndReturnAbsoluteValue";
import { RentalAndReturnData } from "../absoluteValues/rentalsAndReturns/RentalAndReturnData";
import { RentalsAndReturnsPerStation } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationData } from "../absoluteValues/reservations/ReservationData";
import { ReservationsPerStation } from "../absoluteValues/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../absoluteValues/reservations/ReservationsPerUser";
import { EmptyStationInfo, EmptyStateData } from "../absoluteValues/stations/EmptyStationInfo";
import { UserTimeAtSystem, UserTimeData } from '../absoluteValues/users/UserTimeAtSystem';
import * as json2csv from 'json2csv';
import * as fs from 'fs';

export class CsvGenerator {
    
	private readonly NUM_DATA: number = 4;   // number of absolute values of reservation and rental and return data  
    private csvPath: string;
    
    private entityInfoTitles: Array<string>;
    private globalInfoTitles: Array<string>;
    private emptyStationTitles: Array<string>;
    private bikesBalanceTitles: Array<string>;
    private bikesPerStationTitles: Array<string>;
    private userTimeTitles: Array<string>;
    
   	private stationData: Array<JsonObject>;
   	private userData: Array<JsonObject>;
    private globalInfo: JsonObject;
    private emptyStationData: Array<JsonObject>;
    private bikesBalanceData: Array<JsonObject>;
    private bikesPerStationData: Array<JsonObject>;
    private userTimeData: Array<JsonObject>; 
    
    public constructor(csvPath: string) {
        this.csvPath = csvPath;
        this.entityInfoTitles = new Array();
        this.globalInfoTitles = new Array();
        this.emptyStationTitles = new Array();
        this.bikesBalanceTitles = new Array();
        this.bikesPerStationTitles = new Array();
        this.userTimeTitles = new Array();
        this.stationData = new Array();
        this.userData = new Array();
        this.globalInfo = {};
        this.emptyStationData = new Array();
        this.bikesBalanceData = new Array();
        this.bikesPerStationData = new Array();
        this.userTimeData = new Array();
  }
    
  public createJsonFor(entities: Array<Entity>, data: Array<JsonObject>, reservations: SystemInfo, rentalsAndReturns: SystemInfo): void {
    let i = 1;  // title index
    let j = 0;  // data index
      
    for (let entity of entities) {
        let jsonObj: JsonObject = {};
        
        jsonObj.id = entity.id;
        
        while (j < this.NUM_DATA) {   // adding reservations' data
            let reservationValues: AbsoluteValue | undefined = reservations.getData().absoluteValues.get(entity.id);
            if (reservationValues !== undefined) {
                let absValueList: Array<number> = reservationValues.getAbsoluteValuesAsArray();
                jsonObj[this.entityInfoTitles[i]] = absValueList[j]; 
                i++;
                j++;
            }
        }
        
        j = 0;
        while (j < this.NUM_DATA) {   // adding rentals and returns' data
            let rentalAndReturnValues: AbsoluteValue | undefined = rentalsAndReturns.getData().absoluteValues.get(entity.id);
            if (rentalAndReturnValues !== undefined) {
                let absValueList: Array<number> = rentalAndReturnValues.getAbsoluteValuesAsArray();
                jsonObj[this.entityInfoTitles[i]] = absValueList[j];
                i++;   
                j++;
            }
        }
        data.push(jsonObj);
		i = 1;
		j = 0;
    }
  }

    private async initEntityInfoTitles(): Promise<void> {
         this.entityInfoTitles.push('id');
         ReservationData.NAMES.forEach( (name) => this.entityInfoTitles.push(name));
         RentalAndReturnData.NAMES.forEach( (name) => this.entityInfoTitles.push(name));
         return;
    }
     
     private async initStationInfo(info: Map<string, SystemInfo>, stations: Array<Station>): Promise<void> {
         let reservations: SystemInfo | undefined = info.get(ReservationsPerStation.name);
         let rentalsAndReturns: SystemInfo | undefined = info.get(RentalsAndReturnsPerStation.name);
         if (reservations && rentalsAndReturns) {
           this.createJsonFor(stations, this.stationData, reservations, rentalsAndReturns);
         }
         return;
     }
    
    private async initUserInfo(info: Map<string, SystemInfo>, users: Array<User>): Promise<void> {
         let reservations: SystemInfo | undefined = info.get(ReservationsPerUser.name);
         let rentalsAndReturns: SystemInfo | undefined = info.get(RentalsAndReturnsPerUser.name);
         if (reservations && rentalsAndReturns) {   
           this.createJsonFor(users, this.userData, reservations, rentalsAndReturns);
         }
        return;
    }
    
    private async initGlobalInfo(globalInfo: SystemGlobalInfo): Promise<void> {
        SystemGlobalInfo.NAMES.forEach( (name) => this.globalInfoTitles.push(name));
        
        let data: Array<number> = globalInfo.getDataAsArray();
        for(let i = 0; i<data.length; i++) {
            this.globalInfo[this.globalInfoTitles[i]] = data[i];
        }
        return;
    }
    
    private async initEmptyStationInfo(info: Map<string, SystemInfo>): Promise<void> {
        let emptyStations: SystemInfo | undefined = info.get(EmptyStationInfo.name);
        if (emptyStations) {
             EmptyStateData.NAMES.forEach( (name) => {
             this.emptyStationTitles.push(name);
            });
            emptyStations.getData().absoluteValues.forEach( (v, k) => {
                let jsonObj: JsonObject = {};
                jsonObj[this.emptyStationTitles[0]] = v.intervalsToString();
                jsonObj[this.emptyStationTitles[1]] = v.totalTime;
                this.emptyStationData.push(jsonObj);
            });
        }
        return;
    }
    
    private async initBikesBalancingInfo(info: Map<string, SystemInfo>): Promise<void> {
        this.bikesBalanceTitles[0] = 'id';
        this.bikesBalanceTitles[1] = StationBalancingData.NAMES;
        let bikesBalance: SystemInfo | undefined = info.get(StationBalancingQuality.name); 
        if (bikesBalance) {
            bikesBalance.getData().absoluteValues.forEach( (value, stationId) => {
                let obj: JsonObject = {};
                obj[this.bikesBalanceTitles[0]] = stationId;
                obj[this.bikesBalanceTitles[1]] = value.quality;
                this.bikesBalanceData.push(obj);
            });
        }
        return;
    }
    
    private async initUserTimeInfo(info: Map<string, SystemInfo>): Promise<void> {
        this.userTimeTitles[0] = 'id';
        this.userTimeTitles[1] = UserTimeData.NAMES;
        let userInfo: SystemInfo | undefined = info.get(UserTimeAtSystem.name); 
        if (userInfo) {
            userInfo.getData().absoluteValues.forEach( (timeInfo, userId) => {
                let obj: JsonObject = {};
                obj[this.userTimeTitles[0]] = userId;
                obj[this.userTimeTitles[1]] = timeInfo.time;
                this.userTimeData.push(obj);
            });
        }
        return;
    }
    
    private async initBikesPerStationInfo(bikesPerStation: BikesPerStationAndTime): Promise<void> {
        this.bikesPerStationTitles[0] = 'id';
        this.bikesPerStationTitles[1] = 'time';
        this.bikesPerStationTitles[2] = 'available bikes';
        
        bikesPerStation.getStations().forEach( (bikesList, stationId) => {
            let list: Array<BikesPerTime> = bikesList.getList();
            for (let item of list) {
                let obj: JsonObject = {};
                obj[this.bikesPerStationTitles[0]] = stationId;
                obj[this.bikesPerStationTitles[1]] = item.time;
                obj[this.bikesPerStationTitles[2]] = item.availableBikes;
                this.bikesPerStationData.push(obj);
            }
            });
        return;
    } 

	   public async generate(entityInfo: Map<string, SystemInfo>, globalInfo: SystemGlobalInfo, bikesPerStation: BikesPerStationAndTime, stations: Array<Station>, users: Array<User>): Promise<void> {
         await this.initEntityInfoTitles();
           
         await this.initStationInfo(entityInfo, stations);
         this.transformStationJsonToCsv();
           
         await this.initUserInfo(entityInfo, users);
         this.transformUserJsonToCsv();
           
         await this.initGlobalInfo(globalInfo); 
         this.transformGlobalInfoJsonToCsv();
           
         await this.initEmptyStationInfo(entityInfo);
         this.transformEmptyStationJsonToCsv();
        
         await this.initBikesBalancingInfo(entityInfo);
         this.transformBikesBalanceJsonToCsv();
        
         await this.initBikesPerStationInfo(bikesPerStation);
         this.transformBikesPerStationJsonToCsv();
        
         await this.initUserTimeInfo(entityInfo);
         this.transformUserTimeJsonToCsv();
        return;
   	}

	private transformStationJsonToCsv(): void {
        let csv = json2csv({ data: this.stationData, fields: this.entityInfoTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/stations.csv`, csv, (err) => {
          if (err) throw err;
          console.log('stations file saved');
        });
	}
    
    private transformUserJsonToCsv(): void {
        let csv = json2csv({ data: this.userData, fields: this.entityInfoTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/users.csv`, csv, (err) => {
          if (err) throw err;
          console.log('user file saved');
        });
    }

    private transformGlobalInfoJsonToCsv(): void {
        let csv = json2csv({data: [this.globalInfo], fields: this.globalInfoTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile(`${this.csvPath}/global_values.csv`, csv, (err) => {
            if (err) throw err;
            console.log('global values file saved');
        });
    }
    
    private transformEmptyStationJsonToCsv(): void {
        let csv = json2csv({data: this.emptyStationData, fields: this.emptyStationTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile(`${this.csvPath}/empty_stations.csv`, csv, (err) => {
            if (err) throw err;
            console.log('empty stations file saved');
        });        
    }

	private checkFolders(): void {
        if(!fs.existsSync(this.csvPath)) {
            fs.mkdirSync(this.csvPath);
        }
    }
    
    private transformBikesBalanceJsonToCsv(): void {
        let csv = json2csv({ data: this.bikesBalanceData, fields: this.bikesBalanceTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/stationBalancingQuality.csv`, csv, (err) => {
          if (err) throw err;
          console.log('Bikes balance quality file saved');
        });
    }
    
    private transformBikesPerStationJsonToCsv(): void {
        let csv = json2csv({ data: this.bikesPerStationData, fields: this.bikesPerStationTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/bikesPerStationAndTime.csv`, csv, (err) => {
          if (err) throw err;
          console.log('Bikes per station and time file saved');
        });
    }
    
    private transformUserTimeJsonToCsv(): void {
        let csv = json2csv({ data: this.userTimeData, fields: this.userTimeTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/userTimeAtSystem.csv`, csv, (err) => {
          if (err) throw err;
          console.log('User time at system file saved');
        });
    }
   
}  