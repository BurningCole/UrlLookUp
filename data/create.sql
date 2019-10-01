CREATE TABLE websites(
webId INTEGER NOT NULL,
url VARCHAR(75),
accept VARCHAR(20),
exclude VARCHAR(20),
PRIMARY KEY(webId)
);

CREATE TABLE urls(
id INTEGER NOT NULL,
webId INTEGER NOT NULL,
url VARCHAR(150) NOT NULL,
alias VARCHAR(100),
updated DATE,
PRIMARY KEY(id),
FOREIGN KEY(webId) references websites(webId)
);

INSERT INTO urls(webId,url,alias) VALUES 
(0,"hakoiri_drops/chapter_77","Hakoiri Drops"),
(0,"niehime_to_kemono_no_ou1/chapter_44","Sacrifice princess and beast king"),
(0,"wp919114/chapter_6","Useless Ponko (robo-maid)"),
(0,"peter_grill_to_kenja_no_jikan/chapter_14","guy chased by multiple sex-hungry women"),
(0,"gamers/chapter_5","Gamers Aka. Misunderstandings the manga"),
(0,"number_girl/chapter_22","clones work ut how to be individual"),
(0,"versatile_mage/chapter_144","Guy with multiple elemental magic"),
(1,"the_wrong_way_to_use_healing_magic/chapter_21","Healing magic can make you stronk"),
(0,"df918379/chapter_3.2","x777 and lottery tickits isekai"),
(0,"wo919335/chapter_4.1","OP protective dragon mom insists on adventuring with son"),
(0,"kk918448/chapter_5","(pseudo?)Inf power at lvl 2"),
(0,"hn918480/chapter_47","Release that witch"),
(0,"isekai_ni_kita_mitai_dakedo_ikanisureba_yoi_no_darou/chapter_7","guy with analyse-type magic gets married by accident"),
(0,"ss919336/chapter_12","Gyaru anthology"),
(0,"tn918023/chapter_44","pseudo harem by 1 girl"),
(0,"xx917964/chapter_14","brothers turn each other into sisters"),
(0,"wb919369/chapter_3","Golem master");

INSERT INTO websites(url,accept,exclude) VALUES ("http://isekaiscan.com/manga/",'class="btn next_page"','class="read-container"');



