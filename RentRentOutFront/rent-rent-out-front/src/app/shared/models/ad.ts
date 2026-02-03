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
owner: object;
category: object;
location: object;
dateIntervals: string;
}
