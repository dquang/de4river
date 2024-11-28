#!/usr/bin/env ruby
# encoding: UTF-8

# Use like
#   grep -ri getLogger src/ | ruby find_alien_loggers.rb
# Prints guesses about java source files where wrong logger is used to stderr.
ARGF.each_line do |line|
  module_name = line.scan(/\/([^\/]*)\.java/)
  next if module_name.nil?
  next if module_name[0].nil?
  module_name = module_name[0][0]
  STDERR.puts line unless line.include?("#{module_name}.class")
end

