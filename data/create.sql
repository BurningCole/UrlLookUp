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



UPDATE urls SET url='s_rare_soubi_no_niau_kanojo/chapter_6.2' WHERE id = 254;
UPDATE urls SET url='ok918892/chapter_10' WHERE id = 257;
UPDATE urls SET url='yl918521/chapter_10' WHERE id = 263;
UPDATE urls SET url='wp918498/chapter_106' WHERE id = 265;
UPDATE urls SET url='wo919038/chapter_12' WHERE id = 276;
UPDATE urls SET url='rokudou_no_onnatachi/chapter_145' WHERE id = 278;
UPDATE urls SET url='hs919146/chapter_15' WHERE id = 280;
--UPDATE urls SET url='#Change this#' WHERE id = 281;
UPDATE urls SET url='xk919132/chapter_29' WHERE id = 284;
UPDATE urls SET url='kuro_no_maou/chapter_9' WHERE id = 295;
UPDATE urls SET url='wq918170/chapter_36' WHERE id = 299;
UPDATE urls SET url='us918613/chapter_27' WHERE id = 300;
UPDATE urls SET url='from_maid_to_mother/chapter_32' WHERE id = 304;
--UPDATE urls SET url='#Change this#' WHERE id = 305;
UPDATE urls SET url='challenge/internet-explorer/ep-39-microsoft/viewer?title_no=219164&episode_no=61' WHERE id = 307;
UPDATE urls SET url='be918962/chapter_14' WHERE id = 314;
UPDATE urls SET url='xi919082/chapter_12' WHERE id = 317;
UPDATE urls SET url='qg918612/chapter_3.1' WHERE id = 320;
UPDATE urls SET url='mc918834/chapter_9.2' WHERE id = 322;
UPDATE urls SET url='koushaku_reijou_no_tashinami/chapter_46' WHERE id = 323;
UPDATE urls SET url='jb918021/chapter_98' WHERE id = 325;
UPDATE urls SET url='shinka_no_mi/chapter_13' WHERE id = 326;
UPDATE urls SET url='spirit_sword_sovereign/chapter_196' WHERE id = 330;
UPDATE urls SET url='pz918164/chapter_27' WHERE id = 332;
UPDATE urls SET url='tales_of_demons_and_gods/chapter_229.5' WHERE id = 334;
UPDATE urls SET url='tensei_ouji_wa_daraketai/chapter_17' WHERE id = 336;
UPDATE urls SET url='time_lover/chapter_89' WHERE id = 341;
UPDATE urls SET url='ot918876/chapter_14' WHERE id = 342;
UPDATE urls SET url='up919279/chapter_3.3' WHERE id = 346;
UPDATE urls SET url='ip918793/chapter_7.1' WHERE id = 348;
UPDATE urls SET url='honzuki_no_gekokujou/chapter_26' WHERE id = 352;
UPDATE urls SET url='gw918412/chapter_28' WHERE id = 353;
UPDATE urls SET url='challenge/noble-senpai-and-ashley-sans-real-life-adventures/ashley-chan-sugoi-desu/viewer?title_no=263410&episode_no=54' WHERE id = 359;
UPDATE urls SET url='challenge/fox-girls-are-better/episode-25-ultimate/viewer?title_no=201808&episode_no=28' WHERE id = 361;
UPDATE urls SET url='2019/07/01/1094-tribal-wars/' WHERE id = 362;
UPDATE urls SET url='urasai/chapter_21' WHERE id = 364;
UPDATE urls SET url='ore_ga_suki_nano_wa_imouto_dakedo_imouto_ja_nai/chapter_6' WHERE id = 367;
UPDATE urls SET url='yonakano_reijini_haremu_wo/chapter_17' WHERE id = 369;
UPDATE urls SET url='fn919222/chapter_6' WHERE id = 378;
UPDATE urls SET url='peter_grill_to_kenja_no_jikan/chapter_17' WHERE id = 382;
UPDATE urls SET url='number_girl/chapter_26' WHERE id = 384;
UPDATE urls SET url='hn918480/chapter_53' WHERE id = 390;
UPDATE urls SET url='isekai_ni_kita_mitai_dakedo_ikanisureba_yoi_no_darou/chapter_8' WHERE id = 391;
UPDATE urls SET url='ss919336/chapter_22' WHERE id = 392;
UPDATE urls SET url='tn918023/chapter_46' WHERE id = 393;
UPDATE urls SET url='rp918441/chapter_11' WHERE id = 397;
UPDATE urls SET url='ul918437/chapter_5' WHERE id = 402;
UPDATE urls SET url='imouto_sae_ireba_ii__comic/chapter_10' WHERE id = 407;
UPDATE urls SET url='maou_no_mama_ni_narundayo/chapter_12' WHERE id = 408;
UPDATE urls SET url='okaasan_10sai_to_boku/chapter_9' WHERE id = 413;
UPDATE urls SET url='ng919517/chapter_10' WHERE id = 414;
UPDATE urls SET url='ay918540/chapter_24.5' WHERE id = 415;
