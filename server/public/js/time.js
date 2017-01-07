function toNiceTimeStamp( date ) {
	return amPM(date,false) +":"+("0"+date.getMinutes()).slice(-2)+" "+amPM(date,true) +
			" " +
			dayOfWeek( date) ;
			//+ " " +
			//monthOfYear(date) +" " +
			//date.getDate()+ ", " +
			//date.getFullYear() +
			//" at " +
			

}
function niceMins( date ) {
	return ("0" + date.getMinutes()).slice(-2);

}
function dayOfWeek( date ) {
	switch(date.getDay() ) {
		case 0: return "Sunday";
		case 1:	return "Monday"; 
		case 2:	return "Tuesday";
		case 3:	return "Wednesday";
		case 4:	return "Thursday";
		case 5:	return "Friday";
		case 6:	return "Saturday";
		default:return null;
	}
}
function monthOfYear( date ) {

	switch(date.getMonth()) {
		case 0: return "January";
		case 1:	return "February";
		case 2:	return "March";
		case 3:	return "April";
		case 4:	return "May";
		case 5:	return "June";
		case 6:	return "July";
		case 7:	return "August";
		case 8:	return "September";
		case 9:	return "October";
		case 10:return "November";
		case 11:return "December";
		default:return null;
	}
}
function amPM( date, label ) {
	mil = date.getHours();
	mil > 11 ? pm = true : pm = false;
	if( label ) {
		pm ? result = 'PM' : result = 'AM';
	} else {
		pm ? result = mil > 12 ? mil-12 : 12 : result = mil==0 ? 12 : mil;
	}
	return result;
}

/**
 * Date.parse with progressive enhancement for ISO 8601 <https://github.com/csnover/js-iso8601>
 * NON-CONFORMANT EDITION.
 * © 2011 Colin Snover <http://zetafleet.com>
 * Released under MIT license.
 */
(function (Date, undefined) {
    var origParse = Date.parse, numericKeys = [ 1, 4, 5, 6, 10, 11 ];
    Date.parse = function (date) {
        var timestamp, struct, minutesOffset = 0;

        //              1 YYYY                 2 MM        3 DD              4 HH     5 mm        6 ss            7 msec         8 Z 9 ±    10 tzHH    11 tzmm
        if ((struct = /^(\d{4}|[+\-]\d{6})(?:-?(\d{2})(?:-?(\d{2}))?)?(?:[ T]?(\d{2}):?(\d{2})(?::?(\d{2})(?:[,\.](\d{1,}))?)?(?:(Z)|([+\-])(\d{2})(?::?(\d{2}))?)?)?$/.exec(date))) {
            // avoid NaN timestamps caused by “undefined” values being passed to Date.UTC
            for (var i = 0, k; (k = numericKeys[i]); ++i) {
                struct[k] = +struct[k] || 0;
            }

            // allow undefined days and months
            struct[2] = (+struct[2] || 1) - 1;
            struct[3] = +struct[3] || 1;

            // allow arbitrary sub-second precision beyond milliseconds
            struct[7] = struct[7] ? + (struct[7] + "00").substr(0, 3) : 0;

            // timestamps without timezone identifiers should be considered local time
            if (struct[8] === undefined && struct[9] === undefined) {
                timestamp = +new Date(struct[1], struct[2], struct[3], struct[4], struct[5], struct[6], struct[7]);
            }
            else {
                if (struct[8] !== 'Z' && struct[9] !== undefined) {
                    minutesOffset = struct[10] * 60 + struct[11];

                    if (struct[9] === '+') {
                        minutesOffset = 0 - minutesOffset;
                    }
                }

                timestamp = Date.UTC(struct[1], struct[2], struct[3], struct[4], struct[5] + minutesOffset, struct[6], struct[7]);
            }
        }
        else {
            timestamp = origParse ? origParse(date) : NaN;
        }

        return timestamp;
    };
}(Date));