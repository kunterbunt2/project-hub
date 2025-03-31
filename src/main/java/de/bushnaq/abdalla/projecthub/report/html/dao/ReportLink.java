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

package de.bushnaq.abdalla.projecthub.report.html.dao;

public class ReportLink extends ReportCell {

    public String link;

    public ReportLink(String column, String text, String link) {
        super(column, text);
        this.link = link;
    }

    public ReportLink(String column, String text, String link, boolean hidden) {
        super(column, text, hidden);
        this.link = link;
    }

    public ReportLink(String column, String text, String icon, String link) {
        super(column, text, icon);
        this.link = link;
    }

    public ReportLink(String column, String text, String icon, String link, boolean hidden) {
        super(column, text, icon, hidden);
        this.link = link;
    }

}
