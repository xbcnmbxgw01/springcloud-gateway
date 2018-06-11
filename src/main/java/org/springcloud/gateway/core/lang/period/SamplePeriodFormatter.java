/*
 * Copyright 2017 ~ 2025 the original author or authors. <springcloudgateway@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springcloud.gateway.core.lang.period;

import static java.lang.Math.abs;

/**
 * {@link SamplePeriodFormatter}
 *
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @since
 */
public class SamplePeriodFormatter extends PeriodFormatter {

    /**
     * Always print timespan in decimal format.
     */
    private boolean printDecimalAlway = true;

    public boolean isPrintDecimalAlway() {
        return printDecimalAlway;
    }

    public void setPrintDecimalAlway(boolean printDecimalAlway) {
        this.printDecimalAlway = printDecimalAlway;
    }

    @Override
    public String formatHumanDate(long startTime, long endTime) {
        if (startTime > endTime) {
            log.debug("StartTime: {} must be greater than or equal to the endTime: {}", startTime, endTime);
        }

        StringBuffer sb = new StringBuffer();
        long diffSec = (startTime - endTime) / 1000;

        long sec = (diffSec >= 60 ? diffSec % 60 : diffSec);
        long min = (diffSec = (diffSec / 60)) >= 60 ? diffSec % 60 : diffSec;
        long hours = (diffSec = (diffSec / 60)) >= 24 ? diffSec % 24 : diffSec;
        long days = (diffSec = (diffSec / 24)) >= 30 ? diffSec % 30 : diffSec;
        long months = (diffSec = (diffSec / 30)) >= 12 ? diffSec % 12 : diffSec;
        long years = (diffSec = (diffSec / 12));

        // Calculate output negative interval.
        final boolean isAbs = diffSec < 0;
        if (isAbs) {
            sec = abs(sec);
            min = abs(min);
            hours = abs(hours);
            days = abs(days);
            months = abs(months);
            years = abs(years);
        }

        if (years > 0) {
            if (years == 1) {
                sb.append(" ");
                sb.append(getLocalizedMessage("period.formatter.year"));
            } else {
                if (isPrintDecimalAlway()) {
                    sb.append(" ");
                    sb.append(years);
                    if (!isIngoreLowerDate() && months > 0) {
                        sb.append(".");
                        sb.append(months);
                    }
                    sb.append(" ");
                    sb.append(getLocalizedMessage("period.formatter.years"));
                } else {
                    sb.append(" ");
                    sb.append(years);
                    sb.append(" ");
                    sb.append(getLocalizedMessage("period.formatter.years"));
                }
            }
            if (!isPrintDecimalAlway()) {
                if (years <= 6 && months > 0) {
                    if (months == 1) {
                        sb.append(" ");
                        sb.append(getLocalizedMessage("period.formatter.month"));
                    } else {
                        sb.append(" ");
                        sb.append(months);
                        sb.append(" ");
                        sb.append(getLocalizedMessage("period.formatter.months"));
                    }
                }
            }
        } else if (months > 0) {
            if (months == 1) {
                sb.append(" ");
                sb.append(getLocalizedMessage("period.formatter.month"));
            } else {
                if (isPrintDecimalAlway()) {
                    sb.append(" ");
                    sb.append(months);
                    if (!isIngoreLowerDate() && days > 0) {
                        sb.append(".");
                        sb.append(days);
                    }
                    sb.append(" ");
                    sb.append(getLocalizedMessage("period.formatter.months"));
                } else {
                    sb.append(" ");
                    sb.append(months);
                    sb.append(" ");
                    sb.append(getLocalizedMessage("period.formatter.months"));
                }
            }
            if (!isPrintDecimalAlway()) {
                if (months <= 6 && days > 0) {
                    if (days == 1) {
                        sb.append(" ");
                        sb.append(getLocalizedMessage("period.formatter.day"));
                    } else {
                        sb.append(" ");
                        sb.append(days);
                        sb.append(" ");
                        sb.append(getLocalizedMessage("period.formatter.days"));
                    }
                }
            }
        } else if (days > 0) {
            if (days == 1) {
                sb.append(" ");
                sb.append(getLocalizedMessage("period.formatter.day"));
            } else {
                if (isPrintDecimalAlway()) {
                    sb.append(" ");
                    sb.append(days);
                    if (!isIngoreLowerDate() && hours > 0) {
                        sb.append(".");
                        sb.append(hours);
                    }
                    sb.append(" ");
                    sb.append(getLocalizedMessage("period.formatter.days"));
                } else {
                    sb.append(" ");
                    sb.append(days);
                    sb.append(" ");
                    sb.append(getLocalizedMessage("period.formatter.days"));
                }
            }
            if (!isPrintDecimalAlway()) {
                if (days <= 3 && hours > 0) {
                    if (hours == 1) {
                        sb.append(" ");
                        sb.append(getLocalizedMessage("period.formatter.hour"));
                    } else {
                        sb.append(" ");
                        sb.append(hours);
                        sb.append(" ");
                        sb.append(getLocalizedMessage("period.formatter.hours"));
                    }
                }
            }
        } else if (hours > 0) {
            if (hours == 1) {
                sb.append(" ");
                sb.append(getLocalizedMessage("period.formatter.hour"));
            } else {
                if (isPrintDecimalAlway()) {
                    sb.append(" ");
                    sb.append(hours);
                    if (!isIngoreLowerDate() && min > 0) {
                        sb.append(".");
                        sb.append(min);
                    }
                    sb.append(" ");
                    sb.append(getLocalizedMessage("period.formatter.hours"));
                } else {
                    sb.append(" ");
                    sb.append(hours);
                    sb.append(" ");
                    sb.append(getLocalizedMessage("period.formatter.hours"));
                }
            }
            if (!isPrintDecimalAlway()) {
                if (min > 1) {
                    sb.append(" ");
                    sb.append(min);
                    sb.append(" ");
                    sb.append(getLocalizedMessage("period.formatter.minutes"));
                }
            }
        } else if (min > 0) {
            if (min == 1) {
                sb.append(" ");
                sb.append(getLocalizedMessage("period.formatter.minute"));
            } else {
                if (isPrintDecimalAlway()) {
                    sb.append(" ");
                    sb.append(min);
                    if (!isIngoreLowerDate() && sec > 0) {
                        sb.append(".");
                        sb.append(sec);
                    }
                    sb.append(" ");
                    sb.append(getLocalizedMessage("period.formatter.minutes"));
                } else {
                    sb.append(" ");
                    sb.append(min);
                    sb.append(" ");
                    sb.append(getLocalizedMessage("period.formatter.minutes"));
                }
            }
            if (!isIngoreLowerDate() && sec > 1) {
                sb.append(" ");
                sb.append(sec);
                sb.append(" ");
                sb.append(getLocalizedMessage("period.formatter.seconds"));
            }
        } else {
            if (sec <= 30) {
                sb.append(" ");
                sb.append(getLocalizedMessage("period.formatter.just"));
                sb.append(" ");
            } else {
                sb.append(" ");
                sb.append(getLocalizedMessage("period.formatter.about"));
                sb.append(sec);
                sb.append(" ");
                sb.append(getLocalizedMessage("period.formatter.seconds"));
            }
        }
        sb.append(" ".concat(getLocalizedMessage("period.formatter.ago")));

        // Cleanup empty chars
        String periodString = cleanupDateEmptyString(sb.toString());

        // Output negative interval.
        if (isAbs) {
            periodString = "-".concat(periodString);
        }
        return periodString;
    }

}