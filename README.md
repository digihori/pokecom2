# PB-100シミュレータのプロジェクト
# PokecomGO2 - CASIO Pocket computer(PB-100) simulator
#
# This is a simulator of CASIO's Pocket Computer PB-100.
# It reproduces the unique BASIC of the PB-100 generation.
# (Omitting the closing parentheses of calculation formula,
# using variables in the destination of GOTO, etc. ..)
# You can input and output BASIC text file by load and save menu.
#
# ＜Supported BASIC instraction＞
# PRINT
# INPUT
# GOTO
# GOSUB-RETURN
# FOR-TO-STEP-NEXT
# IF-THEN/IF-;
# VAC
# CLEAR/CLEAR A
# RUN
# LIST
# END
# KEY
# CSR
# DEFM
# MID
# VAL
# SIN/COS/TAN/ASN/ACS/ATN
# LOG/LN/EXP/SQR
# ABS
# SGN
# INT
# FRAC
# RND
# RAN#
# STOP
#
# ＜Setting menu＞
# ・cpu clock emulate
# 　Putting some wait of execution and realize the operation close
# 　to the actual machine.
# ・memory unit
# 　Setting whether extended memory exists or not.
# 　It only changes the remaining memory display.
# ・UI design
# 　When set to 'PB-100 compatible' it will be like PB-100.
# 　When set to 'PB-100 custom' the 'F'key and '↑','↓'key is enabled.
# ・viblation
# 　It vibrates when the key is pressed.
# ・debug
# 　It displays debug messages and a frame of software keys.
#
# ＜About Special Symbols＞
# In the input/output file, PB special symbols are converted to escape characters.
# Invalid except ASCII code.
#
# \\  ¥
# \LE(or <=) Less Equal
# \NE(or <>) Not Equal
# \GE(or >=) Greater Equal
# \LA Left Arrow
# \DA Down Arrow
# \RA Right Arrow
# \UA Up Arrow
# \PI PI
# \EX EXponent
# \CI CIrcle
# \SQ SQuare
# \TR TRiangle
# \CR CRoss
# \DV DiVide
# \DT DoT
# \DG DeGree
# \SP SPade
# \HT HearT
# \DI DIa
# \CL CLover
# \BX Box
# \SG SiGma
# \OM OMega
# \MU MU
# 
# ＜Other＞
# Spaces between instructions and parameters can be omitted.
# (ex.)
# PRINT CSR1;"ABC"
# SINA
# etc.
#
# If you press the '←' key after manual calculation,
# the previous command will be displayed.
# Step count calculation is not accurate. There are some errors with the actual machine.
# Program trace execution is not implemented yet.
# Moreover, the program can not resume after interruption. It will only be finished.