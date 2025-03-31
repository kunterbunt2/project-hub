/*
 *
 * Copyright (C) 2025-2025 Abdalla Bushnaq
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.bushnaq.abdalla.profiler;

import de.bushnaq.abdalla.util.date.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeKeeping implements AutoCloseable {
    private       LogLevel      logLevel = LogLevel.NONE;
    private       Logger        logger   = null;
    private       String        subject;
    private final LocalDateTime time     = LocalDateTime.now();

    public TimeKeeping() {
        createLogger();
    }

    public TimeKeeping(String subject) {
        createLogger();
        this.subject = subject;
    }

    public TimeKeeping(String subject, boolean logResult) {
        createLogger();
        this.subject = subject;
        if (logResult) {
            this.logLevel = LogLevel.TRACE;
        }
    }

    public TimeKeeping(String subject, LogLevel logLevel) {
        createLogger();
        this.subject  = subject;
        this.logLevel = logLevel;
    }

    @Override
    public void close() {
        Duration delta = getDelta();
        {
            switch (logLevel) {
                case ERROR:
                    logger.error(String.format("%s in %s.", subject, DateUtil.create24hDurationString(delta, true, true, true, false)));
                    break;
                case INFO:
                    logger.info(String.format("%s in %s.", subject, DateUtil.create24hDurationString(delta, true, true, true, false)));
                    break;
                case NONE:
                    break;
                case TRACE:
                    logger.trace(String.format("%s in %s.", subject, DateUtil.create24hDurationString(delta, true, true, true, false)));
                    break;
                case WARN:
                    logger.warn(String.format("%s in %s.", subject, DateUtil.create24hDurationString(delta, true, true, true, false)));
                    break;
                default:
                    break;
            }

        }
    }

    private void createLogger() {
        String parent = new Exception().getStackTrace()[2].getClassName();
        logger = LoggerFactory.getLogger(parent);
    }

    public Duration getDelta() {
        return Duration.between(time, LocalDateTime.now());
    }

    public void setMessage(String subject) {
        this.subject = subject;
    }

}
