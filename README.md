Adapted from https://github.com/honeynet/HIHAT by Michael Mueter and the HIHAT project (RWTH Aachen University / The Honeynet Project; HIHAT 1.0, also published at https://sourceforge.net/projects/hihat/).

# HIHAT-nx

HIHAT-nx is HIHAT, the High Interaction Honeypot Analysis Toolkit (release 1.0 of July 2007, imported to GitHub by the Honeynet Project in 2019; last upstream commit ad96941), updated so that the Honeypot-Creator, the logging code it inserts and the analysis tool run on current PHP (8.x) with MySQL or MariaDB. The original was written for PHP 4/5 and the `mysql_*` functions, which PHP 7 removed. What HIHAT logs, the database layout and the analysis tool's pages are unchanged. The original README follows below.

## Changes from the original

* `Honeypot-Creator/insertionFile.txt` (the logging code): `mysqli` instead of `mysql_*`, a closure instead of `create_function()` (removed in PHP 8), and no warnings for request bodies without `=`. Database errors still stop the page with the same messages as before.
* `Honeypot-Creator/honeypot_creator.java`:
  * reads `insertionFile.txt`, the name used in the repository and INSTALL (the code looked for `insertionfile.txt`, which fails on case-sensitive file systems); the old name is still accepted;
  * PHP requires `declare(strict_types=1)` and `namespace` statements to come first in a file, so for such files the logging code is placed directly after them instead of in front of the file (otherwise every modern application fails with a fatal error); files that use braced namespace blocks are skipped and listed;
  * files that already contain the logging code are skipped, so running it twice is harmless;
  * the temporary file is written next to the target file;
  * `honeypot_creator.class` is rebuilt for Java 11 and later (`javac --release 11 honeypot_creator.java`).
* `Logserver/HIHAT-Create-Tables.sql`: `ENGINE=` instead of `TYPE=`, `timestamp` instead of `timestamp(14)` (both rejected by current MySQL and MariaDB), a default of 0 for `download_checked` (without it every insert fails in strict SQL mode, the default today), and `attackerIP` widened to 45 characters so that IPv6 addresses fit.
* `Analysis Tool/inc/mysql_compat.php` (new, loaded from `inc/constant.php`): small replacements for the `mysql_*` functions, `ereg()`, `ereg_replace()` and `split()` built on `mysqli` and PCRE, used only where PHP does not provide them.

Not changed: the analysis tool's statistics graphs need JpGraph and its map view needs an ip2location database and a Google Maps API key, as described in INSTALL; neither is bundled.

Tested on Debian 12 (PHP 8.2, MariaDB 10.11, Apache 2.4, OpenJDK 17) with phpMyAdmin 5.2.3 as the web application.

---

# HIHAT
The High Interaction Honeypot Analysis Toolkit

The related paper can be found [here](https://www.researchgate.net/publication/228932600_A_generic_toolkit_for_converting_web_applications_into_high-interaction_honeypots).

NB: Development on this tool has been ceased.

Analysis Tool 	    : Source code of the analysis tool HIHAT
Honeypot-Creator	  : Source code of the Honeypot-Creator
Logserver		        : Script for the creation of database tables at the logserver
INSTALL			        - Installation instructions for the logserver, the honeypots and HIHAT
LICENSE			        - GNU General Public License
README			        - This file


------------------
(c) HIHAT version 1.0

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
