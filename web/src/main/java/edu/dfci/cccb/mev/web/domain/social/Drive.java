/**
 * This program is free software: you can redistribute it and/or modify
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
package edu.dfci.cccb.mev.web.domain.social;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author levk
 * 
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@JsonInclude (Include.NON_NULL)
public class Drive {

  private @Getter final @JsonProperty boolean signedIn;
  private @Getter final @JsonProperty DriveFile[] files;

  @ToString
  @EqualsAndHashCode
  @RequiredArgsConstructor
  public static class DriveFile {
    private @Getter final @JsonProperty String name;
    private @Getter final @JsonProperty String id;
  }
}