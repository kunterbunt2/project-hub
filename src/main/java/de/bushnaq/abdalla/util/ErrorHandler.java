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

package de.bushnaq.abdalla.util;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {

    public List<Throwable> exceptions  = new ArrayList<>();
    public boolean         noException = true;

    protected boolean doubleIsDifferent(double d1, double d2, double delta) {
        return (Double.compare(d1, d2) != 0) && (!(Math.abs(d1 - d2) <= delta));
    }

    public boolean isTrue(String message, boolean value) {
        if (!value) {
            noException = false;
            exceptions.add(new Exception(message));
            return false;
        }
        return true;
    }
}
