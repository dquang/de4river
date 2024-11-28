# run like ruby -i -p assimilate_alien_loggers.rb FILES(or stdin)
# This will replace in-place and will result in wrong getLogger
# statements in certain inner-class scenarios! Be careful.

$last_class
if $_ =~ /class ([a-zA-Z0-9]+)/
  $last_class = "#{$1}.class"
end
if $_ =~ /getLogger/
  gsub(/getLogger[ ]*\(.*\)/, "getLogger(#{$last_class})")
end
