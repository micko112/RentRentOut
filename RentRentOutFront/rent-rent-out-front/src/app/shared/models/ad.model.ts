import {Category} from './category.model';
import {Location} from './location.model';
import {User} from './user.model';
import {DateInterval} from './date-interval.model';
import {UserShort} from './userShort';

export interface Ad {
id: number;
title: string;
description: string;
price: number;
priceInterval: string;
adStatus: string;
totalQuantity: number;
availableQuantity: number;
images: string[];
owner: UserShort;
category: Category;
location: Location;
dateIntervals: string;
blockedIntervals: DateInterval[];
currency: string;
viewCount?: number;
saved?: boolean;
pricePerWeek?: number;
pricePerMonth?: number;

// Detalji nekretnine
advertiserType?: string;
roomCount?: string;
areaSize?: number;
constructionType?: string;
propertyCondition?: string;
totalFloors?: string;
floorNumber?: string;
furnished?: string;
heatingTypes?: string[];
propertyMunicipality?: string;
propertyNeighborhood?: string;
propertyStreet?: string;
landArea?: number;
landAreaUnit?: string;
features?: string[];

// Detalji automobila
carBrand?: string;
carModel?: string;
carYear?: number;
carMileage?: number;
carBodyType?: string;
carFuelType?: string;
carTransmission?: string;
carPowerKw?: number;
carColor?: string;
carDoors?: string;
carSeats?: number;
carDisplacement?: number;
carEmissionClass?: string;
carDrive?: string;
carSteeringWheel?: string;
carRegisteredUntil?: string;
carCountry?: string;
carOrigin?: string;
carOwnership?: string;
carDamage?: string;
carLabel?: string;
carInteriorMaterial?: string;
carInteriorColor?: string;
}
