SignLinkNotify
==============

Bukkit plugin which polls specified directories and files matching the file 
mask `*.var` and reads all lines into [SignLink](https://github.com/bergerkiller/SignLink) variables.
This allows notification from external processes into SignLink variables.

Configuration
-------------

The following example configuration shows `var` files in the
`/home/user/vardir/` directory as well as the file
`/home/user/var_file.var` will be polled for SignLink variables.

    variables:
        file_locations:
            - /home/user/vardir/
            - /home/user/var_file.var
        file_mask: .*\.var

Variable File Format
--------------------

Each line of variable files represent a variable name and value
delimited by `:`.

example SignLink variable called 'text':

    text: Some text to display

Text colour can be specified using colour tokens which are of the form
`%%COLOR%%` where COLOR is the name of a [bukkit chat color](http://jd.bukkit.org/apidocs/org/bukkit/ChatColor.html),
for example the following variable `text` contains the word red in red:

    varname: Message containing %%RED%% red %%RESET%% text.

Sign ticker direction defaults to 'NONE' for variables with a value
less than 14 characters and 'LEFT' otherwise.  You can override this
by providing an optional direction ('LEFT', 'RIGHT' or 'NONE') at the
end of the variable name.  For example the following variable `message`
will scroll right:

    message-RIGHT: This text will scroll this way -->
