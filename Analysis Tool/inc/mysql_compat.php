<?php
/* This file is part of HIHAT-nx (HIHAT v1.1 adapted for PHP 8)
   ================================
   Compatibility layer: PHP 7 removed the mysql functions, ereg and split, and
   PHP 8 made count() throw an error for values that are not arrays.
   The analysis tool was written for them, so this file provides small
   replacements built on mysqli and PCRE. It is only used where PHP does not
   provide the functions itself.

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
*/

if (!function_exists('mysql_connect')) {
    mysqli_report(MYSQLI_REPORT_OFF);
    $GLOBALS['__hihat_mysql_link'] = null;

    function __hihat_link($link = null) {
        return $link ? $link : $GLOBALS['__hihat_mysql_link'];
    }
    function mysql_connect($host = 'localhost', $user = '', $pass = '', $new_link = false) {
        $port = null; $socket = null;
        if (strpos($host, ':') !== false) {
            list($host, $p) = explode(':', $host, 2);
            if (ctype_digit($p)) $port = (int)$p; else $socket = $p;
        }
        $link = @mysqli_connect($host, $user, $pass, '', $port ? $port : 3306, $socket);
        if ($link) {
            mysqli_set_charset($link, 'utf8mb4');
            $GLOBALS['__hihat_mysql_link'] = $link;
        } else {
            $GLOBALS['__hihat_mysql_connect_error'] = mysqli_connect_error();
        }
        return $link ? $link : false;
    }
    function mysql_select_db($db, $link = null) {
        $l = __hihat_link($link);
        return $l ? mysqli_select_db($l, $db) : false;
    }
    function mysql_query($sql, $link = null) {
        $l = __hihat_link($link);
        return $l ? mysqli_query($l, $sql) : false;
    }
    function mysql_error($link = null) {
        $l = __hihat_link($link);
        if (!$l) return isset($GLOBALS['__hihat_mysql_connect_error']) ? $GLOBALS['__hihat_mysql_connect_error'] : 'no connection';
        return mysqli_error($l);
    }
    function mysql_fetch_assoc($r) { return ($r instanceof mysqli_result) ? (mysqli_fetch_assoc($r) ?? false) : false; }
    function mysql_fetch_row($r) { return ($r instanceof mysqli_result) ? (mysqli_fetch_row($r) ?? false) : false; }
    function mysql_fetch_array($r, $type = MYSQLI_BOTH) { return ($r instanceof mysqli_result) ? (mysqli_fetch_array($r, $type) ?? false) : false; }
    function mysql_fetch_object($r) { return ($r instanceof mysqli_result) ? (mysqli_fetch_object($r) ?? false) : false; }
    function mysql_num_rows($r) { return ($r instanceof mysqli_result) ? mysqli_num_rows($r) : 0; }
    function mysql_free_result($r) { if ($r instanceof mysqli_result) mysqli_free_result($r); return true; }
    function mysql_insert_id($link = null) { $l = __hihat_link($link); return $l ? mysqli_insert_id($l) : 0; }
    function mysql_affected_rows($link = null) { $l = __hihat_link($link); return $l ? mysqli_affected_rows($l) : -1; }
    function mysql_real_escape_string($s, $link = null) { $l = __hihat_link($link); return $l ? mysqli_real_escape_string($l, (string)$s) : addslashes((string)$s); }
    function mysql_escape_string($s) { return mysql_real_escape_string($s); }
    function mysql_close($link = null) { $l = __hihat_link($link); if ($l) { mysqli_close($l); if ($l === $GLOBALS['__hihat_mysql_link']) $GLOBALS['__hihat_mysql_link'] = null; } return true; }
}

if (!function_exists('ereg')) {
    function __hihat_posix_re($pattern, $flags = '') {
        return '/' . str_replace('/', '\/', $pattern) . '/' . $flags;
    }
    function ereg($pattern, $string, &$regs = null) {
        return preg_match(__hihat_posix_re($pattern), (string)$string, $regs) === 1;
    }
    function eregi($pattern, $string, &$regs = null) {
        return preg_match(__hihat_posix_re($pattern, 'i'), (string)$string, $regs) === 1;
    }
    function ereg_replace($pattern, $replacement, $string) {
        return preg_replace(__hihat_posix_re($pattern), str_replace('\\', '\\\\', $replacement), (string)$string);
    }
    function eregi_replace($pattern, $replacement, $string) {
        return preg_replace(__hihat_posix_re($pattern, 'i'), str_replace('\\', '\\\\', $replacement), (string)$string);
    }
}

if (!function_exists('split')) {
    function split($pattern, $string, $limit = -1) {
        return preg_split('/' . str_replace('/', '\/', $pattern) . '/', (string)$string, $limit);
    }
}

if (!function_exists('hihat_count')) {
    // count() as in PHP 5 and 7: 0 for null, 1 for other non-countable values (PHP 8 throws a TypeError)
    function hihat_count($value, $mode = COUNT_NORMAL) {
        if (is_array($value) || $value instanceof Countable) return count($value, $mode);
        return $value === null ? 0 : 1;
    }
}
?>
