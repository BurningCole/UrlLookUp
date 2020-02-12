JAVAC  ?= javac

SRC_DIR ?= $(CURDIR)/src
OUT_DIR ?= $(CURDIR)/out
LIB_DIR ?= $(CURDIR)/libs

compile:
	$(JAVAC) -d $(OUT_DIR) -cp $(LIB_DIR)/* -sourcepath $(SRC_DIR) $(SRC_DIR)/*.java
	
jar:
	$(JAVAC) -d $(OUT_DIR) -cp $(LIB_DIR)/* -sourcepath $(SRC_DIR) $(SRC_DIR)/*.java
	jar cfm UrlLookUp.jar Manifest.txt -C $(OUT_DIR) .	
	
run:
	$(JAVAC) -cp $(LIB_DIR)/*;$(OUT_DIR) Main
	
opendb:
	$(LIB_DIR)/sqlite3.exe data/Urls.db
