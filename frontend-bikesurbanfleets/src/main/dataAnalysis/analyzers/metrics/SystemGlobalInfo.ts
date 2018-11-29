
import { User } from "../../systemDataTypes/Entities";
import { SystemInfo } from "../SystemInfo";

export class SystemGlobalInfo {
    static readonly NAMES: Array<string> = [
        'Demand satisfaction',
        'Hire eficiency',
        'Return eficiency'
    ];
    private numberUsers: number;
    private demandSatisfaction: number;
    private hireEfficiency: number;
    private returnEfficiency: number;
    
    public constructor(users: Array<User>) {
        this.numberUsers = users.length;
        
        this.demandSatisfaction = 0;
        this.hireEfficiency = 0;
        this.returnEfficiency = 0;
  }
    
    public calculateGlobalData(reservations: SystemInfo, rentalsAndReturnsPerStation: SystemInfo, rentalsAndReturnsPerUser: SystemInfo): void {
        /*let totalRentals: number = 0;
        let totalReturns: number = 0; 
        let totalBikeReservations: number = 0;
        let totalSlotReservations: number = 0;*/
  
        let successfulRentals: number = 0;
        let successfulReturns: number = 0;
        let successfulBikeReservations: number = 0;
        let successfulSlotReservations: number = 0;  
        
        let failedRentals: number = 0;
        let failedRentalsUsersInSystem: number = 0; // Attempts of those users who hired a bike
        let failedReturns: number = 0;
        let failedBikeReservations: number = 0;
        let failedSlotReservations: number = 0;
            
        reservations.getData().absoluteValues.forEach( (v, k) => successfulBikeReservations += v.successfulBikeReservations); 
        reservations.getData().absoluteValues.forEach( (v, k) => successfulSlotReservations += v.successfulSlotReservations);
        reservations.getData().absoluteValues.forEach( (v, k) => failedBikeReservations += v.failedBikeReservations);
        reservations.getData().absoluteValues.forEach( (v, k) => failedSlotReservations += v.failedSlotReservations);

        //totalBikeReservations = successfulBikeReservations + failedBikeReservations;
        //totalSlotReservations = successfulSlotReservations + failedSlotReservations;

        rentalsAndReturnsPerStation.getData().absoluteValues.forEach( (v, k) => successfulRentals += v.successfulRentals);
        rentalsAndReturnsPerStation.getData().absoluteValues.forEach( (v, k) => successfulReturns += v.successfulReturns);
        rentalsAndReturnsPerStation.getData().absoluteValues.forEach( (v, k) => failedRentals += v.failedRentals);
        rentalsAndReturnsPerStation.getData().absoluteValues.forEach( (v, k) => failedReturns += v.failedReturns);
        rentalsAndReturnsPerUser.getData().absoluteValues.forEach((v, k) => {
            if(v.successfulRentals === 1) {
                failedRentalsUsersInSystem += v.failedRentals;
            }
        });
        console.log("Total failed rentals", failedRentals);
        console.log("Failed rentals of users who take a bike", failedRentalsUsersInSystem);

        //totalRentals = successfulRentals + failedRentals;
        //totalReturns = successfulReturns + failedReturns;

        this.demandSatisfaction = successfulRentals / this.numberUsers;  // DS = SH / N
        this.hireEfficiency = successfulRentals / (successfulRentals + failedRentalsUsersInSystem);  // HE = SH / (FH of users in system)
        this.returnEfficiency = successfulReturns / (successfulRentals + failedReturns); // RE = SR / (SH+FR)
    }

    public getDemandSatisfaction(): number {
        return this.demandSatisfaction;
    }

    public getHireEfficiency(): number {
        return this.hireEfficiency;
    }

    public getReturnEfficiency(): number {
        return this.returnEfficiency;
    }

  public getDataAsArray(): Array<number> {
        let array: Array<number> = new Array();
        array.push(this.demandSatisfaction);
        array.push(this.hireEfficiency);
        array.push(this.returnEfficiency);
        return array;
    }   
   
}