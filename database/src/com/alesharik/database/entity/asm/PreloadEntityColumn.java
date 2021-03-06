/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.database.entity.asm;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Field;

@AllArgsConstructor
@Getter
final class PreloadEntityColumn {
    private final String fieldName;
    private final String columnName;
    private final boolean foreign;
    private final String foreignTable;
    private final String foreignColumn;
    private final boolean indexed;
    private final boolean primary;
    private final boolean unique;
    private final boolean nullable;
    private final String constraint;
    private final String constraintName;
    private final String overrideDomain;
    private final boolean bridge;
    private final boolean lazy;
    /**
     * Field is final
     */
    private final boolean fin;

    public static PreloadEntityColumnBuilder builder() {
        return new PreloadEntityColumnBuilder();
    }

    public EntityColumn build(Class<?> clazz) {
        Field f;
        try {
            f = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            try {
                f = clazz.getField(fieldName);
            } catch (NoSuchFieldException e1) {
                throw new IllegalArgumentException(clazz.toString());
            }
        }
        return new EntityColumn(f, columnName, foreign, foreignTable, foreignColumn, indexed, primary, unique, nullable, constraint, constraintName, overrideDomain, bridge, lazy);
    }

    /**
     * Generated by lombok
     */
    @ToString
    @Generated
    public static class PreloadEntityColumnBuilder {
        private String fieldName = "";
        private String columnName = "";
        private boolean foreign = false;
        private String foreignTable = "";
        private String foreignColumn = "";
        private boolean indexed = false;
        private boolean primary = false;
        private boolean unique = false;
        private boolean nullable = true;
        private boolean fin = false;
        private String constraint = "";
        private String constraintName = "";
        private String overrideDomain = "";
        private boolean bridge = false;
        private boolean lazy = false;

        PreloadEntityColumnBuilder() {
        }

        public PreloadEntityColumn.PreloadEntityColumnBuilder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public PreloadEntityColumn.PreloadEntityColumnBuilder columnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

        public PreloadEntityColumn.PreloadEntityColumnBuilder foreign(boolean foreign) {
            this.foreign = foreign;
            return this;
        }

        public PreloadEntityColumn.PreloadEntityColumnBuilder foreignTable(String foreignTable) {
            this.foreignTable = foreignTable;
            return this;
        }

        public PreloadEntityColumn.PreloadEntityColumnBuilder foreignColumn(String foreignColumn) {
            this.foreignColumn = foreignColumn;
            return this;
        }

        public PreloadEntityColumn.PreloadEntityColumnBuilder indexed(boolean indexed) {
            this.indexed = indexed;
            return this;
        }

        public PreloadEntityColumn.PreloadEntityColumnBuilder primary(boolean primary) {
            this.primary = primary;
            return this;
        }

        public PreloadEntityColumn.PreloadEntityColumnBuilder unique(boolean unique) {
            this.unique = unique;
            return this;
        }

        public PreloadEntityColumn.PreloadEntityColumnBuilder nullable(boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public PreloadEntityColumn.PreloadEntityColumnBuilder fin(boolean fin) {
            this.fin = fin;
            return this;
        }

        public PreloadEntityColumnBuilder constraint(String constraint) {
            this.constraint = constraint;
            return this;
        }

        public PreloadEntityColumnBuilder constraintName(String constraintName) {
            this.constraintName = constraintName;
            return this;
        }

        public PreloadEntityColumnBuilder overrideDomain(String d) {
            this.overrideDomain = d;
            return this;
        }

        public PreloadEntityColumnBuilder lazy(boolean lazy) {
            this.lazy = lazy;
            return this;
        }

        public PreloadEntityColumnBuilder bridge(boolean bridge) {
            this.bridge = bridge;
            return this;
        }

        public PreloadEntityColumn build() {
            if(primary)
                nullable = false;
            if(foreign && foreignTable.isEmpty())
                foreign = false;
            return new PreloadEntityColumn(fieldName, columnName, foreign, foreignTable, foreignColumn, indexed, primary, unique, nullable, constraint, constraintName, overrideDomain, bridge, lazy, false);
        }
    }
}
