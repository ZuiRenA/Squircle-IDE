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

package com.blacksquircle.ui.language.lua

import com.blacksquircle.ui.language.base.Language
import com.blacksquircle.ui.language.base.parser.LanguageParser
import com.blacksquircle.ui.language.base.provider.SuggestionProvider
import com.blacksquircle.ui.language.base.styler.LanguageStyler
import com.blacksquircle.ui.language.lua.parser.LuaParser
import com.blacksquircle.ui.language.lua.provider.LuaProvider
import com.blacksquircle.ui.language.lua.styler.LuaStyler

class LuaLanguage : Language {

    companion object {

        private const val FILE_EXTENSION = ".lua"

        fun supportFormat(fileName: String): Boolean {
            return fileName.endsWith(FILE_EXTENSION, ignoreCase = true)
        }
    }

    override fun getName(): String {
        return "lua"
    }

    override fun getParser(): LanguageParser {
        return LuaParser.getInstance()
    }

    override fun getProvider(): SuggestionProvider {
        return LuaProvider.getInstance()
    }

    override fun getStyler(): LanguageStyler {
        return LuaStyler.getInstance()
    }
}