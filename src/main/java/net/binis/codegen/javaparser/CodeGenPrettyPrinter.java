package net.binis.codegen.javaparser;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 Binis Belev
 * %%
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
 * #L%
 */

import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.PrinterConfiguration;

import java.util.function.Function;

public class CodeGenPrettyPrinter extends DefaultPrettyPrinter {

    private static Function<PrinterConfiguration, VoidVisitor<Void>> createDefaultVisitor() {
        return CodeGenPrettyPrinterVisitor::new;
    }

    private static PrinterConfiguration createDefaultConfiguration() {
        return new DefaultPrinterConfiguration();
    }

    public CodeGenPrettyPrinter() {
        super(createDefaultVisitor(), createDefaultConfiguration());
    }

}
