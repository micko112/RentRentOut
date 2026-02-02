package org.landm.helper;

import java.time.LocalDate;

public record DateInterval (
    	LocalDate from,
    	LocalDate to
    ) {}