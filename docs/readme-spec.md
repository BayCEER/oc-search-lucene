# README File Specification
__ Version 1.4.0 __
__ October 2019 __

1 Naming Convention 
- Scope
	+ Public: __READMEdc.txt__
	+ Private: __READMEdc.private.txt__
- Sorting 
 	__ prefixing can be used to bring README files to a front position in file lists

1.Content
- The content is represented as key:value pairs 
	```
	key:value
	```
- A colon separates key and value
- A key starts at the beginning of a line
- A key is composed of any character, including spaces
	```
	my key1:value
	```
- All keys are optional
- Duplicate keys are allowed and do not overwrite previous values
	```
	key:value1
	key:value2
	```
- All value information is treated as text 
- Multiline values can be indented with spaces:
	```
	key:Line1
	\sLine2
 	```
- Keys with with empty values are ignored
- Any leading and trailing whitespace is removed in keys 
- List values can be separated with space:
	```
	key: value1 value2 value3
	```

1.Example READMEdc.txt:
    ```
    title:Getting started on BayCEER CLOUD
    creator:Oliver Archner
    subject:ownCloud
    subject:BayCEER
    description:A small guide to work with BayCEER CLOUD
     * Installation 
     * Getting started
     * Demo 
    publisher:University of Bayreuth
    contributor:BayCEER IT Group
    date:2019-01-10
    type:Text
    format:Markdown
    identifier:
    source:
    language:en
    relation:
    coverage:
    rights:Creative commons share alike
    ```