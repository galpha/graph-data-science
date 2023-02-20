/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.gds.scaling;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScalerFactoryTest {

    @Test
    void parse() {
        assertThat(ScalarScaler.ScalerFactory.parse("log").name()).isEqualTo(LogScaler.NAME);
        assertThat(ScalarScaler.ScalerFactory.parse("minmax").name()).isEqualTo(MinMax.NAME);
        assertThat(ScalarScaler.ScalerFactory.parse("max").name()).isEqualTo(Max.NAME);
        assertThat(ScalarScaler.ScalerFactory.parse("center").name()).isEqualTo(Center.NAME);
        assertThat(ScalarScaler.ScalerFactory.parse("l1norm").name()).isEqualTo(L1Norm.NAME);
        assertThat(ScalarScaler.ScalerFactory.parse("l2norm").name()).isEqualTo(L2Norm.NAME);
        assertThat(ScalarScaler.ScalerFactory.parse("mean").name()).isEqualTo(Mean.NAME);
        assertThat(ScalarScaler.ScalerFactory.parse("stdscore").name()).isEqualTo(StdScore.NAME);
        assertThat(ScalarScaler.ScalerFactory.parse("none").name()).isEqualTo(NoneScaler.NAME);

        // case insensitive
        assertThat(ScalarScaler.ScalerFactory.parse("L1NORM").name()).isEqualTo(L1Norm.NAME);
        assertThat(ScalarScaler.ScalerFactory.parse("StdScore").name()).isEqualTo(StdScore.NAME);

        // nested syntax
        assertThat(ScalarScaler.ScalerFactory.parse(Map.of("scaler", "log")).name()).isEqualTo(LogScaler.NAME);
        assertThat(ScalarScaler.ScalerFactory.parse(Map.of("scaler", "log", "offset", 10)).name()).isEqualTo(LogScaler.NAME);
        assertThat(ScalarScaler.ScalerFactory.parse(Map.of("scaler", "minmax")).name()).isEqualTo(MinMax.NAME);
        assertThat(ScalarScaler.ScalerFactory.parse(Map.of("scaler", "STDSCORE")).name()).isEqualTo(StdScore.NAME);
        assertThat(ScalarScaler.ScalerFactory.parse(Map.of("scaler", "CEntEr")).name()).isEqualTo(Center.NAME);
    }

    @Test
    void badInput() {
        // bad strings
        assertThatThrownBy(() -> ScalarScaler.ScalerFactory.parse("mean  ")).hasMessageContaining("Unrecognised scaler specified: `mean  `.");
        assertThatThrownBy(() -> ScalarScaler.ScalerFactory.parse("yo")).hasMessageContaining("Unrecognised scaler specified: `yo`.");

        // bad types
        assertThatThrownBy(() -> ScalarScaler.ScalerFactory.parse(1L)).hasMessageContaining("Unrecognised scaler specified: `1`.");
        assertThatThrownBy(() -> ScalarScaler.ScalerFactory.parse(42D)).hasMessageContaining("Unrecognised scaler specified: `42.0`.");
        assertThatThrownBy(() -> ScalarScaler.ScalerFactory.parse(List.of("mean"))).hasMessageContaining("Unrecognised scaler specified: `[mean]`.");
        assertThatThrownBy(() -> ScalarScaler.ScalerFactory.parse(Map.of("mean", "scaler"))).hasMessageContaining("Unrecognised scaler specified: `{mean=scaler}`.");
        assertThatThrownBy(() -> ScalarScaler.ScalerFactory.parse(false)).hasMessageContaining("Unrecognised scaler specified: `false`.");

        // bad nested syntax
        assertThatThrownBy(() -> ScalarScaler.ScalerFactory.parse(Map.of("scaler", "lag"))).hasMessageContaining("Unrecognised scaler specified: `lag`.");
        assertThatThrownBy(() -> ScalarScaler.ScalerFactory.parse(Map.of("scaler", "log", "offset", false))).hasMessageContaining("The value of `offset` must be of type `Number` but was `Boolean`.");
        assertThatThrownBy(() -> ScalarScaler.ScalerFactory.parse(Map.of("scaler", "log", "offsat", 0))).hasMessageContaining("Unexpected configuration key: offsat");
    }

}
