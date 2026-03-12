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
email: string;
owner: UserShort;
category: Category;
location: Location;
dateIntervals: string;
blockedIntervals: DateInterval[];
currency: string;
}
