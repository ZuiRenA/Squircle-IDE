/*
 * Copyright 2022 Squircle IDE contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blacksquircle.ui.language.typescript

import com.blacksquircle.ui.language.base.Language
import com.blacksquircle.ui.language.base.parser.LanguageParser
import com.blacksquircle.ui.language.base.provider.SuggestionProvider
import com.blacksquircle.ui.language.base.styler.LanguageStyler
import com.blacksquircle.ui.language.typescript.parser.TypeScriptParser
import com.blacksquircle.ui.language.typescript.provider.TypeScriptProvider
import com.blacksquircle.ui.language.typescript.styler.TypeScriptStyler

class TypeScriptLanguage : Language {

    companion object {

        private const val FILE_EXTENSION = ".ts"

        fun supportFormat(fileName: String): Boolean {
            return fileName.endsWith(FILE_EXTENSION)
        }
    }

    override fun getName(): String {
        return "typescript"
    }

    override fun getParser(): LanguageParser {
        return TypeScriptParser.getInstance()
    }

    override fun getProvider(): SuggestionProvider {
        return TypeScriptProvider.getInstance()
    }

    override fun getStyler(): LanguageStyler {
        return TypeScriptStyler.getInstance()
    }
}